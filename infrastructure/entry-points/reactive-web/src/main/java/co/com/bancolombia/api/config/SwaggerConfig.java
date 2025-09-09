package co.com.bancolombia.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration for Crediya microservice.
 * Centralizes API documentation configuration following best practices.
 * Clean separation from routing logic following SOLID principles.
 * 
 * Follows Single Responsibility Principle:
 * - Only responsible for API documentation configuration
 * - Separate from business logic and routing
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Configure OpenAPI documentation for the Crediya microservice.
     * Provides comprehensive API documentation with security schemes.
     *
     * @return OpenAPI configuration instance
     */
    @Bean
    public OpenAPI crediyaOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Build API information section for documentation.
     * Contains service description, version, and contact information.
     *
     * @return Info object with API details
     */
    private Info buildApiInfo() {
        return new Info()
                .title("Crediya Orders API")
                .version("v1.0.0")
                .description("Microservicio para gestión de solicitudes de préstamo en Crediya. " +
                           "Proporciona endpoints para crear, consultar y administrar solicitudes de crédito. " +
                           "Arquitectura basada en handlers especializados, principios SOLID y Clean Architecture.")
                .contact(new Contact()
                        .name("Crediya Development Team")
                        .email("dev@crediya.com")
                        .url("https://crediya.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://crediya.com/license"));
    }

    /**
     * Build security components for API documentation.
     * Configures JWT Bearer token authentication scheme.
     *
     * @return Components with security schemes
     */
    private Components buildComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT obtenido del servicio de autenticación"));
    }
}