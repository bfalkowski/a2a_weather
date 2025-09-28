# A2A Weather Agent

A self-contained A2A (Agent-to-Agent) weather agent that provides weather information by ZIP code using OpenWeatherMap API with mock data fallback. This agent follows the A2A protocol and can be deployed to Heroku with full compliance.

## Features

- **A2A Protocol Compliance**: Full A2A (Agent-to-Agent) protocol support with proper agent card schema
- **Weather Information**: Current weather, 5-day forecast, weather alerts, and historical data by ZIP code
- **OpenWeatherMap Integration**: Real weather data when API key is provided
- **Mock Data Fallback**: Realistic mock weather data when API key is not configured or limits are reached
- **Agent Discovery**: REST endpoints for agent card and health checks following A2A standards
- **JSON-RPC Communication**: Full JSON-RPC 2.0 support for agent-to-agent communication
- **Heroku Ready**: Optimized for Heroku deployment with proper port binding and CORS
- **Quarkus Framework**: Fast, lightweight Java framework for microservices
- **A2A Java SDK**: Uses official A2A Java SDK for proper schema compliance

## Available Endpoints

### A2A Agent Discovery
- `GET /` - Root endpoint with agent information and available endpoints
- `GET /agent` - A2A-compliant agent card (primary discovery endpoint)
- `GET /agent/extendedCard` - Extended agent card information
- `GET /agent/authenticatedExtendedCard` - Authenticated agent card information
- `GET /agent/health` - Health check endpoint

### JSON-RPC Communication
- `POST /jsonrpc` - JSON-RPC 2.0 endpoint for A2A protocol methods

#### Available JSON-RPC Methods:

**A2A Protocol Methods:**
- `agent.discover` - Agent discovery with capabilities and skills
- `agent.info` - Basic agent information (name, version, URL, protocol)
- `agent.getCapabilities` - Detailed capability information (streaming, transports, protocols)
- `agent.getSkills` - Available skills with descriptions, tags, and examples
- `agent.health` - Health status with timestamp and platform info
- `agent.status` - Runtime status with uptime and connection info

**Weather Methods:**
- `get_current_weather` - Get current weather conditions by ZIP code
- `get_weather_forecast` - Get 5-day weather forecast by ZIP code
- `get_weather_alerts` - Get weather alerts and warnings for an area
- `get_weather_history` - Get historical weather data by ZIP code

## Local Development

### Prerequisites
- Java 17+
- Maven 3.6+

### Running Locally
```bash
# Build the project
mvn clean package

# Run the application
java -jar target/quarkus-app/quarkus-run.jar
```

The application will be available at `http://localhost:8080`

## Heroku Deployment

### Prerequisites
- Heroku CLI installed
- Git repository with Heroku remote configured

### Deploy to Heroku
```bash
# Add Heroku remote (replace with your app name)
heroku git:remote -a your-weather-agent-name

# Deploy
git push heroku main
```

### Heroku Configuration
The application automatically:
- Uses the `PORT` environment variable provided by Heroku
- Binds to `0.0.0.0` for external access
- Enables CORS for cross-origin requests
- Uses Java 17 runtime

### Optional: OpenWeatherMap API Key
To use real weather data instead of mock data:

```bash
# Set your OpenWeatherMap API key
heroku config:set OPENWEATHER_API_KEY=your_api_key_here
```

**Getting an API Key:**
1. Sign up at https://openweathermap.org/api (free, no credit card required)
2. Get your API key from the dashboard
3. Free tier: 1,000 calls/day, 1 call/minute

## Testing

The repository includes comprehensive test scripts and documentation:

### Quick Test Scripts

#### Bash Script (Comprehensive Testing)
```bash
# Test agent using all weather methods (URL required)
./test-weather-agent.sh https://your-agent.herokuapp.com
```

#### Python Script (Detailed Testing)
```bash
# Test agent with detailed output (URL required)
python3 test_weather_agent.py https://your-agent.herokuapp.com
```

### Manual API Testing

#### A2A Agent Discovery
```bash
# Get A2A-compliant agent card
curl https://your-app.herokuapp.com/agent

# Health check
curl https://your-app.herokuapp.com/agent/health
```

#### Weather Method Examples

**Current Weather:**
```bash
curl -X POST https://your-app.herokuapp.com/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "get_current_weather",
    "params": {
      "zip_code": "10001",
      "country_code": "us"
    },
    "id": 1
  }'
```

**Weather Forecast:**
```bash
curl -X POST https://your-app.herokuapp.com/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "get_weather_forecast",
    "params": {
      "zip_code": "90210",
      "country_code": "us"
    },
    "id": 2
  }'
```

**Weather Alerts:**
```bash
curl -X POST https://your-app.herokuapp.com/jsonrpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "get_weather_alerts",
    "params": {
      "zip_code": "60601",
      "country_code": "us"
    },
    "id": 3
  }'
```

**Weather History:**
```bash
curl -X POST https://your-app.herokuapp.com/jsonrpc \
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
  }'
```

## Project Structure

```
├── src/main/java/io/a2a/examples/weather/
│   ├── AgentCardResource.java    # Agent discovery endpoints
│   ├── JsonRpcResource.java      # JSON-RPC communication with weather methods
│   ├── RootResource.java         # Root endpoint
│   └── WeatherService.java       # Weather API integration and mock data
├── src/main/resources/
│   └── application.properties    # Quarkus configuration
├── test-weather-agent.sh         # Bash test script
├── test_weather_agent.py         # Python test script
├── pom.xml                       # Maven configuration
├── procfile                      # Heroku process definition
├── system.properties             # Java version specification
└── app.json                      # Heroku app configuration
```

## Technology Stack

- **Java 17** - Programming language
- **Quarkus 3.2.9** - Java framework
- **Maven** - Build tool
- **Jakarta REST** - REST API framework
- **JSON-RPC 2.0** - Agent communication protocol
- **A2A Java SDK 0.3.0.Alpha1** - Official A2A protocol implementation
- **OpenWeatherMap API** - Weather data source (optional)
- **Apache HttpClient** - HTTP client for API calls

## Weather Data Sources

### OpenWeatherMap (Real Data)
- **When used**: When `OPENWEATHER_API_KEY` environment variable is set
- **Free tier**: 1,000 calls/day, 1 call/minute
- **Features**: Current weather, 5-day forecast
- **Limitations**: Historical data and alerts require paid plans

### Mock Data (Fallback)
- **When used**: When API key is not configured or API limits are reached
- **Features**: Realistic simulated weather data for all methods
- **Coverage**: Current weather, forecast, alerts, and historical data
- **Reliability**: Always available, no external dependencies

## Supported Countries

The agent supports weather queries for multiple countries:
- **US** (United States) - Primary support
- **CA** (Canada)
- **GB** (United Kingdom)
- **DE** (Germany)
- **FR** (France)
- **IT** (Italy)
- **ES** (Spain)
- **AU** (Australia)
- **JP** (Japan)
- **BR** (Brazil)

## Error Handling

The agent provides comprehensive error handling:

- **Invalid ZIP codes**: Clear error messages with suggestions
- **Missing parameters**: Specific error codes and guidance
- **API failures**: Graceful fallback to mock data
- **Rate limiting**: Automatic fallback when limits are reached
- **Network issues**: Robust error handling with fallbacks

## Rate Limiting and Fallback Strategy

### OpenWeatherMap Limits
- **Free tier**: 1,000 calls/day, 1 call/minute
- **When exceeded**: API returns HTTP 429, agent falls back to mock data
- **No charges**: Account stays free, just blocked until reset

### Mock Data Benefits
- **Always available**: No external dependencies
- **Realistic data**: Simulated weather conditions
- **Full functionality**: All weather methods work with mock data
- **Perfect for demos**: Consistent, reliable responses

## Example Responses

### Current Weather Response
```json
{
  "jsonrpc": "2.0",
  "result": {
    "location": "New York, NY",
    "zip_code": "10001",
    "temperature": 72,
    "feels_like": 75,
    "humidity": 65,
    "pressure": 30.1,
    "wind_speed": 8,
    "wind_direction": 180,
    "condition": "Partly Cloudy",
    "description": "few clouds",
    "visibility": 10,
    "cloudiness": 25,
    "sunrise": 1704110400,
    "sunset": 1704146400,
    "data_source": "OpenWeatherMap",
    "timestamp": 1704110000000
  },
  "id": 1
}
```

### Weather Forecast Response
```json
{
  "jsonrpc": "2.0",
  "result": {
    "location": "Beverly Hills, CA",
    "zip_code": "90210",
    "forecast_days": 5,
    "forecasts": [
      {
        "date_time": "2024-01-01 12:00:00",
        "temperature": 75,
        "feels_like": 78,
        "humidity": 60,
        "condition": "Clear",
        "description": "clear sky",
        "wind_speed": 5,
        "precipitation_chance": 10
      }
    ],
    "data_source": "OpenWeatherMap",
    "timestamp": 1704110000000
  },
  "id": 2
}
```

## Future Enhancements

This agent is designed to be easily extended:

1. **Additional Weather APIs**: Support for multiple weather data sources
2. **Enhanced Historical Data**: Integration with paid weather APIs for historical data
3. **Weather Maps**: Support for weather map data and visualizations
4. **Push Notifications**: Real-time weather alerts and notifications
5. **Caching**: Implement weather data caching for better performance
6. **Analytics**: Weather data analytics and trends
7. **Custom Locations**: Support for coordinates and city names beyond ZIP codes

## License

This project is provided as a demo/template for A2A agent development with weather capabilities.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## Support

For issues and questions:
- Check the test scripts for usage examples
- Review the A2A protocol documentation
- Open an issue in the repository