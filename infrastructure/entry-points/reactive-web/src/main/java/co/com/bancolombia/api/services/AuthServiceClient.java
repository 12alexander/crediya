package co.com.bancolombia.api.services;

import co.com.bancolombia.api.config.ApiPaths;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(WebClient authWebClient) {
        this.webClient = authWebClient;
    }

    public Mono<AuthResponseDTO> validateToken(String token) {
        return webClient.get()
                .uri("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class);
    }

    public Mono<UserReportResponseDTO> getUserByEmailAddress(String token, String email) {
        return webClient.get()
                .uri("/api/v1/users/byEmail/{email}", email)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserReportResponseDTO.class);
    }
}