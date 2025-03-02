package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"regexp"
	"strings"

	"github.com/google/generative-ai-go/genai"
	"github.com/xeipuuv/gojsonschema"
	"google.golang.org/api/option"
)

// Go structs representing the Meal and Workout Plan schema
type Meal struct {
	MealType    string   `json:"meal_type"`
	Name        string   `json:"name"`
	Calories    int      `json:"calories"`
	Macros      Macros   `json:"macros"`
	Ingredients []string `json:"ingredients"`
}

type Macros struct {
	Carbs   int `json:"carbs"`
	Protein int `json:"protein"`
	Fat     int `json:"fat"`
}

type MealPlan struct {
	Meals []Meal `json:"meals"`
}

type Exercise struct {
	Name string `json:"name"`
	Reps int    `json:"reps"`
	Sets int    `json:"sets"`
}

type WorkoutPlan struct {
	WorkoutType     string     `json:"workout_type"`
	DurationMinutes int        `json:"duration_minutes"`
	Exercises       []Exercise `json:"exercises"`
}

// Function to load JSON schema from a file
func loadSchema(filename string) (string, error) {
	data, err := os.ReadFile(filename)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// Function to extract JSON from text (in case Gemini adds explanatory text)
func extractJSON(text string) string {
	// Try to find JSON between curly braces with regex
	re := regexp.MustCompile(`\{[\s\S]*\}`)
	match := re.FindString(text)
	if match != "" {
		return match
	}
	// If regex fails, just return the text as is
	return text
}

func main() {
	// Set up API client
	apiKey := os.Getenv("GEMINI_API_KEY") // Better to use environment variable
	if apiKey == "" {
		log.Fatal("GEMINI_API_KEY environment variable not set")
	}

	ctx := context.Background()
	client, err := genai.NewClient(ctx, option.WithAPIKey(apiKey))
	if err != nil {
		log.Fatal("Error creating client:", err)
	}
	defer client.Close()

	// Define model
	model := client.GenerativeModel("gemini-1.5-flash")
	
	// Configure model for structured output
	model.SetTemperature(0.2) // Lower temperature for more structured output

	// Define endpoints
	http.HandleFunc("/meal-plan", func(w http.ResponseWriter, r *http.Request) {
		mealPlanEndpoint(w, r, model, ctx)
	})

	http.HandleFunc("/workout-plan", func(w http.ResponseWriter, r *http.Request) {
		workoutPlanEndpoint(w, r, model, ctx)
	})

	// Start the server
	port := ":8080"
	fmt.Printf("Server started at %s\n", port)
	log.Fatal(http.ListenAndServe(port, nil))
}

// Endpoint to return a generated meal plan
func mealPlanEndpoint(w http.ResponseWriter, r *http.Request, model *genai.GenerativeModel, ctx context.Context) {
	// Load schema first to avoid redundant loading
	schemaContent, err := loadSchema("schemas/mealSchema.json")
	if err != nil {
		http.Error(w, fmt.Sprintf("Error loading schema: %v", err), http.StatusInternalServerError)
		return
	}

	// Get user prompt from query parameters
	userPrompt := r.URL.Query().Get("prompt")
	
	// Create base prompt with specific instructions for valid JSON format
	basePrompt := `Generate a meal plan in valid JSON format.
	Follow this JSON structure precisely:
	{
		"meals": [
			{
				"meal_type": "Breakfast",
				"name": "Oatmeal with Berries",
				"calories": 350,
				"macros": {"carbs": 60, "protein": 8, "fat": 7},
				"ingredients": ["Oats", "Milk", "Blueberries", "Honey"]
			},
			{
				"meal_type": "Lunch",
				"name": "Grilled Chicken Salad",
				"calories": 500,
				"macros": {"carbs": 20, "protein": 40, "fat": 25},
				"ingredients": ["Chicken", "Lettuce", "Tomatoes", "Olive Oil"]
			},
			{
				"meal_type": "Dinner",
				"name": "Salmon with Quinoa",
				"calories": 600,
				"macros": {"carbs": 30, "protein": 45, "fat": 35},
				"ingredients": ["Salmon", "Quinoa", "Spinach", "Lemon"]
			}
		]
	}
	
	IMPORTANT: Return ONLY the JSON object with no additional text before or after.
	Ensure all numbers are integers, not floating points.
	All meal types must be one of: "Breakfast", "Lunch", "Dinner", or "Snack".
	`

	// Combine user prompt with base prompt if provided
	fullPrompt := basePrompt
	if userPrompt != "" {
		fullPrompt = fmt.Sprintf("Consider the following request: %s\n\n%s", userPrompt, basePrompt)
		log.Printf("Received meal plan request with custom prompt: %s", userPrompt)
	}

	var mealPlan MealPlan
	maxRetries := 3
	var validationErrors []string

	// Try up to maxRetries times to get a valid response
	for attempt := 0; attempt < maxRetries; attempt++ {
		// Generate content from the model
		resp, err := model.GenerateContent(ctx, genai.Text(fullPrompt))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Extract the text content
		textContent, ok := resp.Candidates[0].Content.Parts[0].(genai.Text)
		if !ok {
			http.Error(w, "Invalid response format from model", http.StatusInternalServerError)
			return
		}

		// Extract JSON from the text response
		jsonStr := extractJSON(string(textContent))
		
		// Try to unmarshal the response
		err = json.Unmarshal([]byte(jsonStr), &mealPlan)
		if err != nil {
			validationErrors = append(validationErrors, fmt.Sprintf("Unmarshal error: %v", err))
			continue // Try again
		}

		// Validate against schema
		valid, validationErr := validateAgainstSchema(schemaContent, jsonStr)
		if validationErr != nil {
			validationErrors = append(validationErrors, fmt.Sprintf("Schema validation error: %v", validationErr))
			continue // Try again
		}

		if !valid {
			validationErrors = append(validationErrors, "Failed schema validation")
			continue // Try again
		}

		// If we got here, we have a valid response
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(mealPlan)
		return
	}

	// If we get here, all attempts failed
	http.Error(w, fmt.Sprintf("Failed to generate valid meal plan after %d attempts. Errors: %s", 
		maxRetries, strings.Join(validationErrors, "; ")), http.StatusInternalServerError)
}

// Endpoint to return a generated workout plan
func workoutPlanEndpoint(w http.ResponseWriter, r *http.Request, model *genai.GenerativeModel, ctx context.Context) {
	// Load schema first to avoid redundant loading
	schemaContent, err := loadSchema("schemas/workoutSchema.json")
	if err != nil {
		http.Error(w, fmt.Sprintf("Error loading schema: %v", err), http.StatusInternalServerError)
		return
	}

	// Get user prompt from query parameters
	userPrompt := r.URL.Query().Get("prompt")
	
	// Create base prompt with specific instructions for valid JSON format
	basePrompt := `Generate a workout plan in valid JSON format.
	Follow this JSON structure precisely:
	{
		"workout_type": str,
		"duration_minutes": int,
		"exercises": [
			{"name": "", "reps":  "sets": },
			{"name": "", "reps":  "sets": },
			{"name": "", "reps":  "sets": },
			{"name": "", "reps":  "sets": }
		]
	}
	
	IMPORTANT: Return ONLY the JSON object with no additional text before or after.
	Ensure all numbers are integers, not floating points.
	The workout_type must be one of: "Strength", "Cardio", "Flexibility", or "Mixed".
	Duration must be at least 10 minutes.
	Include at least 3 different exercises with appropriate reps and sets.
	`

	// Combine user prompt with base prompt if provided
	fullPrompt := basePrompt
	if userPrompt != "" {
		fullPrompt = fmt.Sprintf("Consider the following request: %s\n\n%s", userPrompt, basePrompt)
		log.Printf("Received workout plan request with custom prompt: %s", userPrompt)
	}

	var workoutPlan WorkoutPlan
	maxRetries := 3
	var validationErrors []string

	// Try up to maxRetries times to get a valid response
	for attempt := 0; attempt < maxRetries; attempt++ {
		// Generate content from the model
		resp, err := model.GenerateContent(ctx, genai.Text(fullPrompt))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Extract the text content
		textContent, ok := resp.Candidates[0].Content.Parts[0].(genai.Text)
		if !ok {
			http.Error(w, "Invalid response format from model", http.StatusInternalServerError)
			return
		}

		// Extract JSON from the text response
		jsonStr := extractJSON(string(textContent))
		
		// Try to unmarshal the response
		err = json.Unmarshal([]byte(jsonStr), &workoutPlan)
		if err != nil {
			validationErrors = append(validationErrors, fmt.Sprintf("Unmarshal error: %v", err))
			continue // Try again
		}

		// Validate against schema
		valid, validationErr := validateAgainstSchema(schemaContent, jsonStr)
		if validationErr != nil {
			validationErrors = append(validationErrors, fmt.Sprintf("Schema validation error: %v", validationErr))
			continue // Try again
		}

		if !valid {
			validationErrors = append(validationErrors, "Failed schema validation")
			continue // Try again
		}

		// If we got here, we have a valid response
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(workoutPlan)
		return
	}

	// If we get here, all attempts failed
	http.Error(w, fmt.Sprintf("Failed to generate valid workout plan after %d attempts. Errors: %s", 
		maxRetries, strings.Join(validationErrors, "; ")), http.StatusInternalServerError)
}

// Function to validate JSON against a schema
func validateAgainstSchema(schemaContent string, jsonStr string) (bool, error) {
	schemaLoader := gojsonschema.NewStringLoader(schemaContent)
	documentLoader := gojsonschema.NewStringLoader(jsonStr)

	result, err := gojsonschema.Validate(schemaLoader, documentLoader)
	if err != nil {
		return false, err
	}

	if !result.Valid() {
		var errors []string
		for _, desc := range result.Errors() {
			errors = append(errors, desc.String())
		}
		log.Printf("Validation errors: %s", strings.Join(errors, ", "))
	}

	return result.Valid(), nil
}