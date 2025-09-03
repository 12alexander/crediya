package co.com.bancolombia.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

/**
 * Security headers configuration for the Crediya application.
 * Adds security headers to all HTTP responses to enhance application security.
 * 
 * @author Crediya Development Team
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Creates a web filter that adds security headers to all responses.
     * 
     * @return WebFilter that adds security headers
     */
    @Bean
    public WebFilter securityHeadersWebFilter() {
        return (exchange, chain) -> {
            var response = exchange.getResponse();
            var headers = response.getHeaders();
            
            // Prevent clickjacking attacks
            headers.add("X-Frame-Options", "DENY");
            
            // Enable XSS protection
            headers.add("X-XSS-Protection", "1; mode=block");
            
            // Prevent MIME type sniffing
            headers.add("X-Content-Type-Options", "nosniff");
            
            // Referrer policy for privacy
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Content Security Policy
            headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none';"
            );
            
            // Strict Transport Security (HTTPS only)
            // Note: Only add this if the application runs over HTTPS
            // headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            
            // Feature policy to restrict access to browser features
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