package co.com.bancolombia.api.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@AllArgsConstructor
@Component
public class ApiPaths {
    private static final String baseURL = "/api/v1";

    public static final String VALIDATE = baseURL + "/auth/validate";
    public static final String USERSBYEMAIL = baseURL + "/users/byEmail/{email}";
}