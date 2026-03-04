package com.example.hubspot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${HUBSPOT_PRIVATE_APP_TOKEN}")
    private String token;

    @Bean
    public WebClient hubSpotWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.hubapi.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
