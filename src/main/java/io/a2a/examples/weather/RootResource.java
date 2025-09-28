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

@Path("/")
public class RootResource {

    @Inject
    UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoot() {
        Map<String, Object> root = new HashMap<>();
        root.put("name", "A2A Weather Agent");
        root.put("description", "An A2A (Agent-to-Agent) weather agent that provides weather information by ZIP code");
        root.put("version", "1.0.0");
        root.put("protocol", "A2A-0.3.0");
        root.put("status", "UP");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("agent", getBaseUrl() + "/agent");
        endpoints.put("agentCard", getBaseUrl() + "/agent");
        endpoints.put("extendedCard", getBaseUrl() + "/agent/extendedCard");
        endpoints.put("authenticatedCard", getBaseUrl() + "/agent/authenticatedExtendedCard");
        endpoints.put("health", getBaseUrl() + "/agent/health");
        endpoints.put("jsonrpc", getBaseUrl() + "/jsonrpc");
        root.put("endpoints", endpoints);
        
        root.put("capabilities", List.of(
            "Current Weather by ZIP Code",
            "5-Day Weather Forecast",
            "Weather Alerts and Warnings",
            "Historical Weather Data",
            "Mock Data Fallback",
            "OpenWeatherMap Integration"
        ));
        
        root.put("supportedCountries", List.of("US", "CA", "GB", "DE", "FR", "IT", "ES", "AU", "JP", "BR"));
        root.put("dataSources", List.of("OpenWeatherMap", "Mock Data"));
        
        Map<String, String> documentation = new HashMap<>();
        documentation.put("readme", "https://github.com/your-org/a2a-weather-agent");
        documentation.put("apiDocs", getBaseUrl() + "/agent");
        documentation.put("examples", getBaseUrl() + "/agent/extendedCard");
        root.put("documentation", documentation);
        
        Map<String, String> contact = new HashMap<>();
        contact.put("email", "weather-agent@example.com");
        contact.put("website", getBaseUrl());
        root.put("contact", contact);
        
        root.put("timestamp", System.currentTimeMillis());

        return Response.ok(root).build();
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
