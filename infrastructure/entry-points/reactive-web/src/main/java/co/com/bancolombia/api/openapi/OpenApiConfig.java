package co.com.bancolombia.api.openapi;

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
 * Clean separation from routing logic.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI creatediyaOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Crediya Orders API")
                .version("v1.0.0")
                .description("Microservicio para gestión de solicitudes de préstamo en Crediya. " +
                           "Proporciona endpoints para crear, consultar y administrar solicitudes de crédito. " +
                           "Arquitectura basada en handlers especializados y principios SOLID.")
                .contact(new Contact()
                        .name("Crediya Development Team")
                        .email("dev@crediya.com")
                        .url("https://crediya.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://crediya.com/license"));
    }

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