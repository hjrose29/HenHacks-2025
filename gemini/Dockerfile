# Start with the latest official Golang image as our base
FROM golang:alpine AS builder

# Set working directory
WORKDIR /app

# Install git and other dependencies (needed for go get)
RUN apk add --no-cache git

# Copy go.mod and go.sum files first (if they exist)
COPY go.mod go.sum* ./

# Download dependencies
RUN go mod download

# Copy the source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux go build -o fitnessapi .

# Create a minimal image for the final application
FROM alpine:latest

# Install ca-certificates for HTTPS requests
RUN apk --no-cache add ca-certificates

# Set working directory
WORKDIR /root/

# Copy the binary from the builder stage
COPY --from=builder /app/fitnessapi .

# Copy schema files
COPY --from=builder /app/schemas/ ./schemas/

# Expose port 8080
EXPOSE 8080

# Set environment variable for API key (this will be overridden at runtime)
ENV GEMINI_API_KEY=""

# Run the application
CMD ["./fitnessapi"]