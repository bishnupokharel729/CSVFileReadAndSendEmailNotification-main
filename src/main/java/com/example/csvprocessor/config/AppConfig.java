package com.example.csvprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    private final AppProperties appProperties;

    public AppConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public WebClient zippopotamusWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(appProperties.getApiBaseUrl()).build();
    }
}
