package com.example.csvprocessor.client;

import com.example.csvprocessor.model.ZippopotamusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class ZippopotamusClient {

    private final WebClient webClient;

    public ZippopotamusClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public String resolveState(String zipCode) {
        if (zipCode == null || zipCode.isBlank()) {
            return "UNKNOWN";
        }

        try {
            return webClient.get()
                    .uri("/{zipCode}", zipCode)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(ZippopotamusResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(throwable -> throwable instanceof WebClientResponseException || throwable instanceof IllegalStateException))
                    .map(response -> response.getState() != null ? response.getState() : "UNKNOWN")
                    .onErrorResume(error -> {
                        log.warn("Failed to resolve state for zip code {}: {}", zipCode, error.getMessage());
                        return Mono.just("UNKNOWN");
                    })
                    .blockOptional()
                    .orElse("UNKNOWN");
        } catch (Exception e) {
            log.warn("Unexpected failure while resolving zip code {}: {}", zipCode, e.getMessage());
            return "UNKNOWN";
        }
    }
}
