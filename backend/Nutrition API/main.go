package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
	"strings"
	"time"

	"github.com/gorilla/mux"
	"github.com/rs/cors"
)

// Config holds the API credentials
type Config struct {
	ClientID     string
	ClientSecret string
	BaseURL      string
}

// TokenResponse represents the OAuth token response
type TokenResponse struct {
	AccessToken string `json:"access_token"`
	TokenType   string `json:"token_type"`
	ExpiresIn   int    `json:"expires_in"`
	Scope       string `json:"scope"`
}

// APIClient handles API requests
type APIClient struct {
	config      Config
	httpClient  *http.Client
	tokenExpiry time.Time
	token       string
}

// NewAPIClient creates a new API client
func NewAPIClient(config Config) *APIClient {
	return &APIClient{
		config:     config,
		httpClient: &http.Client{Timeout: 10 * time.Second},
	}
}

// GetToken fetches or refreshes the OAuth token
func (c *APIClient) GetToken() (string, error) {
	// Return existing token if it's still valid
	if c.token != "" && time.Now().Before(c.tokenExpiry) {
		return c.token, nil
	}

	// Otherwise, get a new token
	data := url.Values{}
	data.Set("grant_type", "client_credentials")
	data.Set("scope", "basic")

	req, err := http.NewRequest("POST", "https://oauth.fatsecret.com/connect/token",
		strings.NewReader(data.Encode()))
	if err != nil {
		return "", err
	}

	req.SetBasicAuth(c.config.ClientID, c.config.ClientSecret)
	req.Header.Add("Content-Type", "application/x-www-form-urlencoded")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("failed to get token: %s", resp.Status)
	}

	var tokenResp TokenResponse
	if err := json.NewDecoder(resp.Body).Decode(&tokenResp); err != nil {
		return "", err
	}

	// Save token and expiry time
	c.token = tokenResp.AccessToken
	c.tokenExpiry = time.Now().Add(time.Duration(tokenResp.ExpiresIn) * time.Second)

	return c.token, nil
}

// SearchFoods searches for foods in FatSecret API
func (c *APIClient) SearchFoods(query string) ([]byte, error) {
	token, err := c.GetToken()
	if err != nil {
		return nil, fmt.Errorf("authentication error: %w", err)
	}

	params := url.Values{}
	params.Add("method", "foods.search")
	params.Add("search_expression", query)
	params.Add("format", "json")

	requestURL := fmt.Sprintf("%s?%s", c.config.BaseURL, params.Encode())
	req, err := http.NewRequest("GET", requestURL, nil)
	if err != nil {
		return nil, err
	}

	req.Header.Add("Authorization", fmt.Sprintf("Bearer %s", token))
	req.Header.Add("Content-Type", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API error: %s", resp.Status)
	}

	// Read the response body
	var result map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return nil, err
	}

	// Convert back to JSON for the client
	return json.Marshal(result)
}

// Server handles HTTP requests
type Server struct {
	client *APIClient
	router *mux.Router
}

// NewServer creates a new server
func NewServer(client *APIClient) *Server {
	s := &Server{
		client: client,
		router: mux.NewRouter(),
	}
	s.routes()
	return s
}

// routes sets up the routes
func (s *Server) routes() {
	s.router.HandleFunc("/search", s.handleSearch()).Methods("GET")
}

// handleSearch handles the search endpoint
func (s *Server) handleSearch() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query().Get("query")
		if query == "" {
			http.Error(w, "Missing 'query' parameter", http.StatusBadRequest)
			return
		}

		result, err := s.client.SearchFoods(query)
		if err != nil {
			http.Error(w, "Error searching foods: "+err.Error(), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.Write(result)
	}
}

func main() {
	// Read configuration from environment variables
	config := Config{
		ClientID:     os.Getenv("FATSECRET_CLIENT_ID"),
		ClientSecret: os.Getenv("FATSECRET_CLIENT_SECRET"),
		BaseURL:      "https://platform.fatsecret.com/rest/server.api",
	}

	// Validate required configuration
	if config.ClientID == "" || config.ClientSecret == "" {
		log.Fatal("FATSECRET_CLIENT_ID and FATSECRET_CLIENT_SECRET must be set")
	}

	// Create API client and server
	client := NewAPIClient(config)
	server := NewServer(client)

	// Get frontend URL from environment
	frontendURL := os.Getenv("FRONTEND_URL")
	if frontendURL == "" {
		log.Println("Warning: FRONTEND_URL not set, using default CORS configuration")
	}

	// Setup CORS middleware
	corsMiddleware := cors.New(cors.Options{
		AllowedOrigins:   []string{frontendURL},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Content-Type", "Authorization"},
		AllowCredentials: true,
		// Optional: Enable Debugging for testing, consider disabling in production
		Debug: false,
	})

	// Start the server with CORS middleware
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	addr := fmt.Sprintf(":%s", port)
	log.Printf("Server starting on %s", addr)
	log.Printf("CORS enabled for origin: %s", frontendURL)
	log.Fatal(http.ListenAndServe(addr, corsMiddleware.Handler(server.router)))
}
