package io.a2a.examples.weather;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Path("/jsonrpc")
public class JsonRpcResource {

    @Inject
    WeatherService weatherService;

    @Inject
    UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> handleJsonRpc(Map<String, Object> request) {
        String method = (String) request.get("method");
        Object params = request.get("params");
        Object id = request.get("id");

        Object result;
        switch (method) {
            // Standard A2A Protocol Methods
            case "agent.discover":
                result = Map.of(
                    "name", "Weather A2A Agent",
                    "description", "An A2A agent that provides weather information by ZIP code using OpenWeatherMap API with mock data fallback",
                    "version", "1.0.0",
                    "protocolVersion", "0.3.0",
                    "capabilities", Map.of(
                        "streaming", false,
                        "pushNotifications", false,
                        "stateTransitionHistory", false
                    ),
                    "skills", List.of(
                        Map.of("id", "get_current_weather", "name", "Current Weather", "description", "Get current weather conditions by ZIP code"),
                        Map.of("id", "get_weather_forecast", "name", "Weather Forecast", "description", "Get 5-day weather forecast by ZIP code"),
                        Map.of("id", "get_weather_alerts", "name", "Weather Alerts", "description", "Get weather alerts and warnings for an area"),
                        Map.of("id", "get_weather_history", "name", "Weather History", "description", "Get historical weather data by ZIP code")
                    )
                );
                break;
            case "agent.info":
                result = Map.of(
                    "name", "Weather A2A Agent",
                    "description", "An A2A agent that provides weather information by ZIP code using OpenWeatherMap API with mock data fallback",
                    "version", "1.0.0",
                    "url", getBaseUrl(),
                    "protocolVersion", "0.3.0"
                );
                break;
            case "agent.getCapabilities":
                result = Map.of(
                    "streaming", false,
                    "pushNotifications", false,
                    "stateTransitionHistory", false,
                    "supportedTransports", List.of("JSONRPC"),
                    "supportedProtocols", List.of("A2A-0.3.0")
                );
                break;
            case "agent.getSkills":
                result = List.of(
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
                );
                break;
            case "agent.health":
                result = Map.of(
                    "status", "UP",
                    "platform", "Heroku",
                    "timestamp", System.currentTimeMillis(),
                    "version", "1.0.0"
                );
                break;
            case "agent.status":
                result = Map.of(
                    "status", "UP",
                    "uptime", "running",
                    "lastHealthCheck", System.currentTimeMillis(),
                    "activeConnections", 0,
                    "weatherMode", weatherService != null && weatherService.isConfigured() ? "OpenWeatherMap" : "MOCK"
                );
                break;
            // Weather Methods
            case "get_current_weather":
                result = getCurrentWeather(params);
                break;
            case "get_weather_forecast":
                result = getWeatherForecast(params);
                break;
            case "get_weather_alerts":
                result = getWeatherAlerts(params);
                break;
            case "get_weather_history":
                result = getWeatherHistory(params);
                break;
            default:
                return Map.of(
                    "jsonrpc", "2.0",
                    "error", Map.of(
                        "code", -32601,
                        "message", "Method not found: " + method
                    ),
                    "id", id
                );
        }

        return Map.of(
            "jsonrpc", "2.0",
            "result", result,
            "id", id
        );
    }

    private Map<String, Object> getCurrentWeather(Object params) {
        if (!(params instanceof Map)) {
            return Map.of(
                "error", "Invalid input: params must be an object",
                "error_code", "INVALID_INPUT"
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) params;
        String zipCode = (String) paramMap.get("zip_code");
        String countryCode = (String) paramMap.get("country_code");

        if (zipCode == null || zipCode.trim().isEmpty()) {
            return Map.of(
                "error", "Invalid input: zip_code is required",
                "error_code", "MISSING_ZIP_CODE",
                "suggestion", "Please provide a valid ZIP code (e.g., '10001')"
            );
        }

        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "us"; // Default to US
        }

        try {
            return weatherService.getCurrentWeather(zipCode, countryCode);
        } catch (Exception e) {
            return Map.of(
                "error", "Failed to retrieve weather data: " + e.getMessage(),
                "error_code", "WEATHER_API_ERROR"
            );
        }
    }

    private Map<String, Object> getWeatherForecast(Object params) {
        if (!(params instanceof Map)) {
            return Map.of("error", "Invalid input: params must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) params;
        String zipCode = (String) paramMap.get("zip_code");
        String countryCode = (String) paramMap.get("country_code");

        if (zipCode == null || zipCode.trim().isEmpty()) {
            return Map.of(
                "error", "Invalid input: zip_code is required",
                "error_code", "MISSING_ZIP_CODE"
            );
        }

        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "us";
        }

        try {
            return weatherService.getWeatherForecast(zipCode, countryCode);
        } catch (Exception e) {
            return Map.of(
                "error", "Failed to retrieve weather forecast: " + e.getMessage(),
                "error_code", "WEATHER_API_ERROR"
            );
        }
    }

    private Map<String, Object> getWeatherAlerts(Object params) {
        if (!(params instanceof Map)) {
            return Map.of("error", "Invalid input: params must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) params;
        String zipCode = (String) paramMap.get("zip_code");
        String countryCode = (String) paramMap.get("country_code");

        if (zipCode == null || zipCode.trim().isEmpty()) {
            return Map.of(
                "error", "Invalid input: zip_code is required",
                "error_code", "MISSING_ZIP_CODE"
            );
        }

        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "us";
        }

        // Mock weather alerts (OpenWeatherMap doesn't have a free alerts API)
        return getMockWeatherAlerts(zipCode, countryCode);
    }

    private Map<String, Object> getWeatherHistory(Object params) {
        if (!(params instanceof Map)) {
            return Map.of("error", "Invalid input: params must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) params;
        String zipCode = (String) paramMap.get("zip_code");
        String countryCode = (String) paramMap.get("country_code");
        String startDate = (String) paramMap.get("start_date");
        String endDate = (String) paramMap.get("end_date");

        if (zipCode == null || zipCode.trim().isEmpty()) {
            return Map.of(
                "error", "Invalid input: zip_code is required",
                "error_code", "MISSING_ZIP_CODE"
            );
        }

        if (countryCode == null || countryCode.trim().isEmpty()) {
            countryCode = "us";
        }

        // Mock weather history (OpenWeatherMap historical data requires paid plan)
        return getMockWeatherHistory(zipCode, countryCode, startDate, endDate);
    }

    private Map<String, Object> getMockWeatherAlerts(String zipCode, String countryCode) {
        Map<String, Object> alerts = new HashMap<>();
        List<Map<String, Object>> alertList = new ArrayList<>();
        
        // Mock some weather alerts
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("type", "Heat Advisory");
        alert1.put("severity", "Moderate");
        alert1.put("description", "High temperatures expected. Stay hydrated and avoid prolonged outdoor activities.");
        alert1.put("start_time", System.currentTimeMillis());
        alert1.put("end_time", System.currentTimeMillis() + 86400000); // 24 hours
        alertList.add(alert1);
        
        Map<String, Object> alert2 = new HashMap<>();
        alert2.put("type", "Air Quality Alert");
        alert2.put("severity", "Low");
        alert2.put("description", "Moderate air quality. Sensitive groups should limit outdoor activities.");
        alert2.put("start_time", System.currentTimeMillis());
        alert2.put("end_time", System.currentTimeMillis() + 43200000); // 12 hours
        alertList.add(alert2);
        
        alerts.put("location", getMockLocation(zipCode));
        alerts.put("zip_code", zipCode);
        alerts.put("country_code", countryCode);
        alerts.put("alerts", alertList);
        alerts.put("alert_count", alertList.size());
        alerts.put("data_source", "Mock Data (Weather alerts API not available in free tier)");
        alerts.put("timestamp", System.currentTimeMillis());
        
        return alerts;
    }

    private Map<String, Object> getMockWeatherHistory(String zipCode, String countryCode, String startDate, String endDate) {
        Map<String, Object> history = new HashMap<>();
        List<Map<String, Object>> historyList = new ArrayList<>();
        
        // Generate mock historical data for the last 7 days
        long currentTime = System.currentTimeMillis();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> dayHistory = new HashMap<>();
            long dayTime = currentTime - (i * 86400000); // i days ago
            
            dayHistory.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(dayTime)));
            dayHistory.put("high_temperature", 65 + (int)(Math.random() * 20)); // 65-85°F
            dayHistory.put("low_temperature", 45 + (int)(Math.random() * 15)); // 45-60°F
            dayHistory.put("average_temperature", 55 + (int)(Math.random() * 15)); // 55-70°F
            dayHistory.put("humidity", 40 + (int)(Math.random() * 40)); // 40-80%
            dayHistory.put("precipitation", Math.random() * 0.5); // 0-0.5 inches
            dayHistory.put("condition", new String[]{"Clear", "Partly Cloudy", "Cloudy", "Rain"}[new java.util.Random().nextInt(4)]);
            historyList.add(dayHistory);
        }
        
        history.put("location", getMockLocation(zipCode));
        history.put("zip_code", zipCode);
        history.put("country_code", countryCode);
        history.put("start_date", startDate != null ? startDate : "7 days ago");
        history.put("end_date", endDate != null ? endDate : "today");
        history.put("historical_data", historyList);
        history.put("data_points", historyList.size());
        history.put("data_source", "Mock Data (Historical weather API requires paid plan)");
        history.put("timestamp", System.currentTimeMillis());
        
        return history;
    }

    private String getMockLocation(String zipCode) {
        // Simple mock location mapping for common ZIP codes
        Map<String, String> zipToLocation = Map.of(
            "10001", "New York, NY",
            "90210", "Beverly Hills, CA", 
            "60601", "Chicago, IL",
            "33101", "Miami, FL",
            "98101", "Seattle, WA",
            "75201", "Dallas, TX",
            "30301", "Atlanta, GA",
            "02101", "Boston, MA",
            "85001", "Phoenix, AZ",
            "80201", "Denver, CO"
        );
        
        return zipToLocation.getOrDefault(zipCode, "Unknown City, US");
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
