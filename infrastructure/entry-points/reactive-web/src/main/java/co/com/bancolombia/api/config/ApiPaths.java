package co.com.bancolombia.api.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@AllArgsConstructor
@Component
public class ApiPaths {
    private static final String baseURL = "/api/v1";

    // External Services Configuration
    @Value("${external.services.auth.base-url:http://localhost:8090}")
    private static String authServiceBaseUrl;

    public static final String VALIDATE = baseURL + "/auth/validate";
    public static final String USERSBYEMAIL = baseURL + "/users/byEmail/{email}";
    
    // Method to get configured base URL for auth service
    public static String getAuthServiceBaseUrl() {
        return authServiceBaseUrl != null ? authServiceBaseUrl : "http://localhost:8090";
    }
}