package co.com.bancolombia.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public WebFilter securityHeadersWebFilter() {
        return (exchange, chain) -> {
            var response = exchange.getResponse();
            var headers = response.getHeaders();
            
            headers.add("X-Frame-Options", "DENY");
            headers.add("X-XSS-Protection", "1; mode=block");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
            
            headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none';"
            );
            
            headers.add("Permissions-Policy", 
                "accelerometer=(), " +
                "camera=(), " +
                "geolocation=(), " +
                "gyroscope=(), " +
                "magnetometer=(), " +
                "microphone=(), " +
                "payment=(), " +
                "usb=()"
            );
            
            return chain.filter(exchange);
        };
    }
}