package com.artisan.review.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Value("${integration.user-service.url:http://localhost:8080}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public UserProfile getUserProfile(String userId) {
        try {
            return webClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserProfile.class)
                    .block();
        } catch (Exception e) {
            return new UserProfile(userId, "Unknown", null);
        }
    }

    public record UserProfile(String id, String displayName, String avatarUrl) {}
}
