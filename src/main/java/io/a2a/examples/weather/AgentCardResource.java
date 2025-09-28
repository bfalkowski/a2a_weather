package io.a2a.examples.weather;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Path("/agent")
public class AgentCardResource {

    @Inject
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentCard() {
        Map<String, Object> agentCard = Map.of(
            "name", "Weather A2A Agent",
            "description", "An A2A agent that provides weather information by ZIP code using OpenWeatherMap API with mock data fallback",
            "version", "1.0.0",
            "url", getBaseUrl(),
            "protocolVersion", "0.3.0",
            "capabilities", Map.of(
                "streaming", false,
                "pushNotifications", false,
                "stateTransitionHistory", false,
                "supportedTransports", List.of("JSONRPC"),
                "supportedProtocols", List.of("A2A-0.3.0")
            ),
            "skills", List.of(
                Map.of(
                    "id", "get_current_weather",
                    "name", "Current Weather",
                    "description", "Get current weather conditions including temperature, humidity, wind, and conditions by ZIP code",
                    "tags", List.of("weather", "current", "temperature", "conditions"),
                    "examples", List.of("get_current_weather zip_code='10001' country_code='us'")
                ),
                Map.of(
                    "id", "get_weather_forecast",
                    "name", "Weather Forecast", 
                    "description", "Get 5-day weather forecast with detailed daily conditions and precipitation chances",
                    "tags", List.of("weather", "forecast", "5-day", "precipitation"),
                    "examples", List.of("get_weather_forecast zip_code='10001' country_code='us'")
                ),
                Map.of(
                    "id", "get_weather_alerts",
                    "name", "Weather Alerts",
                    "description", "Get weather alerts, warnings, and advisories for a specific area",
                    "tags", List.of("weather", "alerts", "warnings", "advisories"),
                    "examples", List.of("get_weather_alerts zip_code='10001' country_code='us'")
                ),
                Map.of(
                    "id", "get_weather_history",
                    "name", "Weather History",
                    "description", "Get historical weather data for a specific date range and location",
                    "tags", List.of("weather", "history", "historical", "data"),
                    "examples", List.of("get_weather_history zip_code='10001' country_code='us' start_date='2024-01-01' end_date='2024-01-31'")
                )
            ),
            "contact", Map.of(
                "email", "weather-agent@example.com",
                "website", getBaseUrl()
            ),
            "metadata", Map.of(
                "created", "2024-01-01T00:00:00Z",
                "updated", "2024-01-01T00:00:00Z",
                "author", "A2A Weather Agent Team",
                "license", "MIT"
            )
        );

        return Response.ok(agentCard).build();
    }

    @GET
    @Path("/extendedCard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExtendedAgentCard() {
        Map<String, Object> extendedCard = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> agentCardData = (Map<String, Object>) getAgentCard().getEntity();
        extendedCard.putAll(agentCardData);
        
        // Add extended information
        extendedCard.put("extendedInfo", Map.of(
            "supportedCountries", List.of("US", "CA", "GB", "DE", "FR", "IT", "ES", "AU", "JP", "BR"),
            "supportedLanguages", List.of("en", "es", "fr", "de", "it"),
            "dataSources", List.of("OpenWeatherMap", "Mock Data"),
            "updateFrequency", "Real-time",
            "maxRequestsPerDay", 1000,
            "rateLimit", "1 request per minute",
            "dataRetention", "24 hours"
        ));
        
        extendedCard.put("endpoints", Map.of(
            "jsonrpc", getBaseUrl() + "/jsonrpc",
            "health", getBaseUrl() + "/agent/health",
            "status", getBaseUrl() + "/agent/status"
        ));

        return Response.ok(extendedCard).build();
    }

    @GET
    @Path("/authenticatedExtendedCard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthenticatedExtendedAgentCard() {
        Map<String, Object> authCard = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> extendedCardData = (Map<String, Object>) getExtendedAgentCard().getEntity();
        authCard.putAll(extendedCardData);
        
        // Add authentication information
        authCard.put("authentication", Map.of(
            "required", false,
            "methods", List.of("none"),
            "description", "No authentication required for basic weather queries"
        ));
        
        authCard.put("apiKey", Map.of(
            "required", false,
            "description", "OpenWeatherMap API key can be configured for real weather data, otherwise mock data is used",
            "environmentVariable", "OPENWEATHER_API_KEY"
        ));

        return Response.ok(authCard).build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "platform", "Heroku",
            "timestamp", System.currentTimeMillis(),
            "version", "1.0.0",
            "uptime", "running",
            "lastHealthCheck", System.currentTimeMillis(),
            "activeConnections", 0,
            "weatherMode", System.getenv("OPENWEATHER_API_KEY") != null && !System.getenv("OPENWEATHER_API_KEY").isEmpty() ? "OpenWeatherMap" : "MOCK"
        );

        return Response.ok(health).build();
    }

    private String getBaseUrl() {
        try {
            // Get the base URL from the request
            String scheme = uriInfo.getRequestUri().getScheme();
            String host = uriInfo.getRequestUri().getHost();
            int port = uriInfo.getRequestUri().getPort();
            
            // Force HTTPS on Heroku
            if (host != null && host.endsWith(".herokuapp.com")) {
                return "https://" + host;
            }
            
            // Build the base URL
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(scheme).append("://").append(host);
            
            // Only add port if it's not the default port and not -1 (which means use default)
            if (port != -1 && ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443))) {
                baseUrl.append(":").append(port);
            }
            
            return baseUrl.toString();
        } catch (Exception e) {
            // Fallback to environment variable or default
            String herokuUrl = System.getenv("HEROKU_APP_NAME");
            if (herokuUrl != null && !herokuUrl.isEmpty()) {
                return "https://" + herokuUrl + ".herokuapp.com";
            }
            
            // Final fallback
            return "https://a2a-weather-agent.herokuapp.com";
        }
    }
}
