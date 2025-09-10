package co.com.bancolombia.api.services;

import co.com.bancolombia.api.config.ApiPaths;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(ApiPaths.getAuthServiceBaseUrl()).build();
    }

    public Mono<AuthResponseDTO> validateToken(String token) {
        return webClient.get()
                .uri(ApiPaths.VALIDATE)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class);
    }

    public Mono<UserReportResponseDTO> getUserByEmailAddress(String token, String email) {
        log.info("Llamando al servicio de autenticación para obtener usuario por email: {}", email);
        log.debug("URL base del servicio de auth: {}", ApiPaths.getAuthServiceBaseUrl());
        log.debug("Path completo: {}", ApiPaths.USERSBYEMAIL);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.USERSBYEMAIL)
                        .build(email))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserReportResponseDTO.class)
                .doOnSuccess(user -> log.info("Usuario obtenido exitosamente del servicio de auth: {}", user != null ? user.getEmailAddress() : "null"))
                .doOnError(error -> log.error("Error al obtener usuario del servicio de auth para email {}: {}", email, error.getMessage(), error));
    }
}