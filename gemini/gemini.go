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
	"github.com/gorilla/mux"
	"github.com/google/generative-ai-go/genai"
	"github.com/xeipuuv/gojsonschema"
	"google.golang.org/api/option"
)

// Go structs representing the Meal and Workout Plan schema
type Meal struct {
	MealType    string   `json:"meal_type"`
	Name        string   `json:"name"`
	Description     string     `json:"description"` 
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
	Description     string     `json:"description"` 
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
	
	model.SetTemperature(.9)
	model.SetTopP(0.95)
	
	// Define endpoints
	r := mux.NewRouter()

    // Define endpoints
    r.HandleFunc("/meal-plan", func(w http.ResponseWriter, r *http.Request) {
        mealPlanEndpoint(w, r, model, ctx)
    }).Methods("GET", "POST")

    r.HandleFunc("/workout-plan", func(w http.ResponseWriter, r *http.Request) {
        workoutPlanEndpoint(w, r, model, ctx)
    }).Methods("GET", "POST")

	r.Use(corsMiddleware)
	
	// Start the server
	port := ":8080"
	fmt.Printf("Server started at %s\n", port)
	log.Fatal(http.ListenAndServe(port, r))
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

	data, err := os.ReadFile("prompts/meal_base_prompt.txt")
	if err != nil {
		fmt.Println("Error reading file:", err)
		return
	}
	content := string(data)

	basePrompt := content

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

	// Get prompt content based on request method
	var userPrompt string
	
	if r.Method == "POST" {
		// For POST requests, read the JSON body
		if r.Header.Get("Content-Type") == "application/json" {
			// Limit request body size to prevent abuse
			r.Body = http.MaxBytesReader(w, r.Body, 1048576) // 1MB limit
			
			// Parse the JSON request body
			var requestData map[string]interface{}
			decoder := json.NewDecoder(r.Body)
			decoder.DisallowUnknownFields()
			
			if err := decoder.Decode(&requestData); err != nil {
				http.Error(w, fmt.Sprintf("Error parsing JSON request: %v", err), http.StatusBadRequest)
				return
			}
			
			// Convert the JSON object to a string representation
			jsonBytes, err := json.Marshal(requestData)
			if err != nil {
				http.Error(w, fmt.Sprintf("Error processing request data: %v", err), http.StatusInternalServerError)
				return
			}
			
			userPrompt = string(jsonBytes)
			log.Printf("Received workout plan POST request with JSON: %s", userPrompt)
		} else {
			http.Error(w, "Content-Type must be application/json for POST requests", http.StatusBadRequest)
			return
		}
	}

	// Load base prompt from file
	data, err := os.ReadFile("prompts/work_base_prompt.txt")
	if err != nil {
		http.Error(w, fmt.Sprintf("Error reading base prompt: %v", err), http.StatusInternalServerError)
		return
	}
	basePrompt := string(data)

	// Combine user input with base prompt if provided
	fullPrompt := basePrompt
	if userPrompt != "" {
		fullPrompt = fmt.Sprintf("Consider the following request MAKE SURE YOU ADD A DESCRIPTION: %s\n\n%s", userPrompt, basePrompt)
	}

	var workoutPlan WorkoutPlan
	maxRetries := 10
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
		log.Printf(fullPrompt)
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

// corsMiddleware adds CORS headers to the response
func corsMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        // Allow requests from localhost:3000
        w.Header().Set("Access-Control-Allow-Origin", "*")
        w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
        w.Header().Set("Access-Control-Allow-Credentials", "true")

        // Handle preflight OPTIONS request
        if r.Method == http.MethodOptions {
            return
        }

        next.ServeHTTP(w, r)
    })
}
