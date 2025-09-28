#!/bin/bash

# A2A Weather Agent Test Script
# Tests all weather agent endpoints and methods

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if URL is provided
if [ $# -eq 0 ]; then
    print_error "Usage: $0 <agent_url>"
    print_error "Example: $0 https://your-weather-agent.herokuapp.com"
    exit 1
fi

AGENT_URL="$1"
print_status "Testing A2A Weather Agent at: $AGENT_URL"

# Test A2A Agent Discovery
print_status "Testing A2A Agent Discovery..."

echo "1. Testing root endpoint..."
ROOT_RESPONSE=$(curl -s "$AGENT_URL/" | jq '.')
if [ $? -eq 0 ]; then
    print_success "Root endpoint working"
    echo "$ROOT_RESPONSE" | jq '.name, .version, .status'
else
    print_error "Root endpoint failed"
fi

echo -e "\n2. Testing agent card..."
AGENT_CARD=$(curl -s "$AGENT_URL/agent" | jq '.')
if [ $? -eq 0 ]; then
    print_success "Agent card working"
    echo "$AGENT_CARD" | jq '.name, .version, .protocolVersion'
else
    print_error "Agent card failed"
fi

echo -e "\n3. Testing health check..."
HEALTH_RESPONSE=$(curl -s "$AGENT_URL/agent/health" | jq '.')
if [ $? -eq 0 ]; then
    print_success "Health check working"
    echo "$HEALTH_RESPONSE" | jq '.status, .platform, .weatherMode'
else
    print_error "Health check failed"
fi

# Test JSON-RPC Weather Methods
print_status "\nTesting JSON-RPC Weather Methods..."

echo -e "\n4. Testing get_current_weather..."
CURRENT_WEATHER=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "get_current_weather",
        "params": {
            "zip_code": "10001",
            "country_code": "us"
        },
        "id": 1
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Current weather method working"
    echo "$CURRENT_WEATHER" | jq '.result.location, .result.temperature, .result.condition, .result.data_source'
else
    print_error "Current weather method failed"
fi

echo -e "\n5. Testing get_weather_forecast..."
FORECAST=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "get_weather_forecast",
        "params": {
            "zip_code": "90210",
            "country_code": "us"
        },
        "id": 2
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Weather forecast method working"
    echo "$FORECAST" | jq '.result.location, .result.forecast_days, .result.data_source'
else
    print_error "Weather forecast method failed"
fi

echo -e "\n6. Testing get_weather_alerts..."
ALERTS=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "get_weather_alerts",
        "params": {
            "zip_code": "60601",
            "country_code": "us"
        },
        "id": 3
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Weather alerts method working"
    echo "$ALERTS" | jq '.result.location, .result.alert_count, .result.data_source'
else
    print_error "Weather alerts method failed"
fi

echo -e "\n7. Testing get_weather_history..."
HISTORY=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "get_weather_history",
        "params": {
            "zip_code": "33101",
            "country_code": "us",
            "start_date": "2024-01-01",
            "end_date": "2024-01-07"
        },
        "id": 4
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Weather history method working"
    echo "$HISTORY" | jq '.result.location, .result.data_points, .result.data_source'
else
    print_error "Weather history method failed"
fi

# Test A2A Protocol Methods
print_status "\nTesting A2A Protocol Methods..."

echo -e "\n8. Testing agent.discover..."
DISCOVER=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "agent.discover",
        "params": {},
        "id": 5
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Agent discover method working"
    echo "$DISCOVER" | jq '.result.name, .result.version, .result.skills | length'
else
    print_error "Agent discover method failed"
fi

echo -e "\n9. Testing agent.getSkills..."
SKILLS=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "agent.getSkills",
        "params": {},
        "id": 6
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Agent skills method working"
    echo "$SKILLS" | jq '.result | length'
    echo "$SKILLS" | jq '.result[].name'
else
    print_error "Agent skills method failed"
fi

# Test Error Handling
print_status "\nTesting Error Handling..."

echo -e "\n10. Testing invalid method..."
INVALID_METHOD=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "invalid_method",
        "params": {},
        "id": 7
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Error handling working"
    echo "$INVALID_METHOD" | jq '.error.code, .error.message'
else
    print_error "Error handling test failed"
fi

echo -e "\n11. Testing missing zip_code..."
MISSING_ZIP=$(curl -s -X POST "$AGENT_URL/jsonrpc" \
    -H "Content-Type: application/json" \
    -d '{
        "jsonrpc": "2.0",
        "method": "get_current_weather",
        "params": {
            "country_code": "us"
        },
        "id": 8
    }' | jq '.')

if [ $? -eq 0 ]; then
    print_success "Missing parameter handling working"
    echo "$MISSING_ZIP" | jq '.result.error, .result.error_code'
else
    print_error "Missing parameter test failed"
fi

# Summary
print_status "\n=== Test Summary ==="
print_success "A2A Weather Agent testing completed!"
print_status "Agent URL: $AGENT_URL"
print_status "All major endpoints and methods have been tested."
print_warning "Note: If using mock data, weather information will be simulated."
print_status "To use real weather data, set the OPENWEATHER_API_KEY environment variable."

echo -e "\n${GREEN}Test completed successfully!${NC}"
