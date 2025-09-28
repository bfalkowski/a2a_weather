package io.a2a.examples.weather;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Optional;

@ApplicationScoped
public class WeatherService {

    @Inject
    @ConfigProperty(name = "openweather.api.key")
    Optional<String> apiKey;

    @Inject
    @ConfigProperty(name = "openweather.api.base.url", defaultValue = "https://api.openweathermap.org/data/2.5")
    String baseUrl;

    @Inject
    @ConfigProperty(name = "openweather.api.timeout", defaultValue = "5000")
    int timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public boolean isConfigured() {
        return apiKey.isPresent() && !apiKey.get().trim().isEmpty();
    }

    public Map<String, Object> getCurrentWeather(String zipCode, String countryCode) {
        if (!isConfigured()) {
            return getMockCurrentWeather(zipCode);
        }

        try {
            String url = String.format("%s/weather?zip=%s,%s&appid=%s&units=imperial", 
                                    baseUrl, zipCode, countryCode, apiKey.get());
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "A2A-Weather-Agent/1.0");
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return parseOpenWeatherResponse(responseBody, zipCode);
                    } else if (response.getStatusLine().getStatusCode() == 429) {
                        // Rate limit exceeded, fall back to mock
                        return getMockCurrentWeather(zipCode);
                    } else {
                        // API error, fall back to mock
                        return getMockCurrentWeather(zipCode);
                    }
                }
            }
        } catch (Exception e) {
            // Any error, fall back to mock
            return getMockCurrentWeather(zipCode);
        }
    }

    public Map<String, Object> getWeatherForecast(String zipCode, String countryCode) {
        if (!isConfigured()) {
            return getMockWeatherForecast(zipCode);
        }

        try {
            String url = String.format("%s/forecast?zip=%s,%s&appid=%s&units=imperial", 
                                    baseUrl, zipCode, countryCode, apiKey.get());
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "A2A-Weather-Agent/1.0");
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return parseOpenWeatherForecastResponse(responseBody, zipCode);
                    } else {
                        return getMockWeatherForecast(zipCode);
                    }
                }
            }
        } catch (Exception e) {
            return getMockWeatherForecast(zipCode);
        }
    }

    private Map<String, Object> parseOpenWeatherResponse(String responseBody, String zipCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            Map<String, Object> weather = new HashMap<>();
            weather.put("location", root.path("name").asText() + ", " + root.path("sys").path("country").asText());
            weather.put("zip_code", zipCode);
            weather.put("temperature", root.path("main").path("temp").asDouble());
            weather.put("feels_like", root.path("main").path("feels_like").asDouble());
            weather.put("humidity", root.path("main").path("humidity").asInt());
            weather.put("pressure", root.path("main").path("pressure").asDouble());
            weather.put("wind_speed", root.path("wind").path("speed").asDouble());
            weather.put("wind_direction", root.path("wind").path("deg").asInt());
            weather.put("condition", root.path("weather").get(0).path("main").asText());
            weather.put("description", root.path("weather").get(0).path("description").asText());
            weather.put("visibility", root.path("visibility").asInt());
            weather.put("cloudiness", root.path("clouds").path("all").asInt());
            weather.put("sunrise", root.path("sys").path("sunrise").asLong());
            weather.put("sunset", root.path("sys").path("sunset").asLong());
            weather.put("data_source", "OpenWeatherMap");
            weather.put("timestamp", System.currentTimeMillis());
            
            return weather;
        } catch (Exception e) {
            return getMockCurrentWeather(zipCode);
        }
    }

    private Map<String, Object> parseOpenWeatherForecastResponse(String responseBody, String zipCode) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode city = root.path("city");
            JsonNode forecasts = root.path("list");
            
            List<Map<String, Object>> forecastList = new ArrayList<>();
            
            for (JsonNode forecast : forecasts) {
                Map<String, Object> dayForecast = new HashMap<>();
                dayForecast.put("date_time", forecast.path("dt_txt").asText());
                dayForecast.put("temperature", forecast.path("main").path("temp").asDouble());
                dayForecast.put("feels_like", forecast.path("main").path("feels_like").asDouble());
                dayForecast.put("humidity", forecast.path("main").path("humidity").asInt());
                dayForecast.put("condition", forecast.path("weather").get(0).path("main").asText());
                dayForecast.put("description", forecast.path("weather").get(0).path("description").asText());
                dayForecast.put("wind_speed", forecast.path("wind").path("speed").asDouble());
                dayForecast.put("precipitation_chance", forecast.path("pop").asDouble() * 100);
                forecastList.add(dayForecast);
            }
            
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("location", city.path("name").asText() + ", " + city.path("country").asText());
            forecast.put("zip_code", zipCode);
            forecast.put("forecast_days", forecastList.size());
            forecast.put("forecasts", forecastList);
            forecast.put("data_source", "OpenWeatherMap");
            forecast.put("timestamp", System.currentTimeMillis());
            
            return forecast;
        } catch (Exception e) {
            return getMockWeatherForecast(zipCode);
        }
    }

    private Map<String, Object> getMockCurrentWeather(String zipCode) {
        Map<String, Object> weather = new HashMap<>();
        
        // Generate realistic mock data
        String[] conditions = {"Clear", "Partly Cloudy", "Cloudy", "Rain", "Snow", "Fog", "Thunderstorm"};
        String[] descriptions = {"clear sky", "few clouds", "scattered clouds", "broken clouds", 
                               "shower rain", "rain", "thunderstorm", "snow", "mist"};
        
        String condition = conditions[random.nextInt(conditions.length)];
        String description = descriptions[random.nextInt(descriptions.length)];
        
        // Temperature based on season (rough approximation)
        int baseTemp = 70 + (random.nextInt(40) - 20); // 50-90°F range
        int feelsLike = baseTemp + (random.nextInt(6) - 3); // ±3°F variation
        
        weather.put("location", getMockLocation(zipCode));
        weather.put("zip_code", zipCode);
        weather.put("temperature", baseTemp);
        weather.put("feels_like", feelsLike);
        weather.put("humidity", 30 + random.nextInt(50)); // 30-80%
        weather.put("pressure", 29.5 + random.nextDouble() * 1.0); // 29.5-30.5 inHg
        weather.put("wind_speed", random.nextDouble() * 15); // 0-15 mph
        weather.put("wind_direction", random.nextInt(360));
        weather.put("condition", condition);
        weather.put("description", description);
        weather.put("visibility", 5 + random.nextInt(6)); // 5-10 miles
        weather.put("cloudiness", random.nextInt(101)); // 0-100%
        weather.put("sunrise", System.currentTimeMillis() - 3600000); // 1 hour ago
        weather.put("sunset", System.currentTimeMillis() + 3600000); // 1 hour from now
        weather.put("data_source", "Mock Data (API key not configured)");
        weather.put("timestamp", System.currentTimeMillis());
        
        return weather;
    }

    private Map<String, Object> getMockWeatherForecast(String zipCode) {
        Map<String, Object> forecast = new HashMap<>();
        List<Map<String, Object>> forecastList = new ArrayList<>();
        
        String[] conditions = {"Clear", "Partly Cloudy", "Cloudy", "Rain", "Snow"};
        String[] descriptions = {"clear sky", "few clouds", "scattered clouds", "broken clouds", "shower rain"};
        
        for (int i = 0; i < 5; i++) {
            Map<String, Object> dayForecast = new HashMap<>();
            String condition = conditions[random.nextInt(conditions.length)];
            String description = descriptions[random.nextInt(descriptions.length)];
            
            dayForecast.put("date_time", "2024-01-" + String.format("%02d", i + 1) + " 12:00:00");
            dayForecast.put("temperature", 65 + random.nextInt(20)); // 65-85°F
            dayForecast.put("feels_like", 65 + random.nextInt(20));
            dayForecast.put("humidity", 40 + random.nextInt(40)); // 40-80%
            dayForecast.put("condition", condition);
            dayForecast.put("description", description);
            dayForecast.put("wind_speed", random.nextDouble() * 12); // 0-12 mph
            dayForecast.put("precipitation_chance", random.nextDouble() * 60); // 0-60%
            forecastList.add(dayForecast);
        }
        
        forecast.put("location", getMockLocation(zipCode));
        forecast.put("zip_code", zipCode);
        forecast.put("forecast_days", 5);
        forecast.put("forecasts", forecastList);
        forecast.put("data_source", "Mock Data (API key not configured)");
        forecast.put("timestamp", System.currentTimeMillis());
        
        return forecast;
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
}
