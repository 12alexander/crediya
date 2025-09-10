package co.com.bancolombia.api.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class ApiPaths {
    private static final String baseURL = "/api/v1";

    // External Services Configuration
    @Value("${external.services.auth.base-url:http://localhost:8090}")
    private String authServiceBaseUrl;

    public static final String VALIDATE = baseURL + "/auth/validate";
    public static final String USERSBYEMAIL = baseURL + "/users/byEmail/{email}";
    
    // Static instance to access configured values
    private static ApiPaths instance;
    
    public ApiPaths() {
        instance = this;
    }
    
    // Method to get configured base URL for auth service
    public static String getAuthServiceBaseUrl() {
        return instance != null && instance.authServiceBaseUrl != null ? 
            instance.authServiceBaseUrl : "http://localhost:8090";
    }
}