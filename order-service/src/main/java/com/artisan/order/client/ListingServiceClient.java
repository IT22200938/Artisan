package com.artisan.order.client;

import com.artisan.order.client.dto.StockCheckResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Component
public class ListingServiceClient {

    private final WebClient webClient;

    public ListingServiceClient(@Value("${integration.listing-service.url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public StockCheckResponse checkStock(String listingId, int quantity) {
        try {
            return webClient.post()
                    .uri("/api/listings/stock/check")
                    .bodyValue(new StockCheckRequest(listingId, quantity))
                    .retrieve()
                    .bodyToMono(StockCheckResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new IllegalStateException("Failed to check stock for listing " + listingId + ": " + e.getMessage());
        }
    }

    public void reduceStock(String listingId, int quantity) {
        webClient.post()
                .uri("/api/listings/stock/reduce")
                .bodyValue(new StockReduceRequest(listingId, quantity))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ListingInfo getListing(String listingId) {
        return webClient.get()
                .uri("/api/listings/{id}", listingId)
                .retrieve()
                .bodyToMono(ListingInfo.class)
                .block();
    }

    private record StockCheckRequest(String listingId, int quantity) {}
    private record StockReduceRequest(String listingId, int quantity) {}

    public record ListingInfo(String id, String title, String sellerId, java.math.BigDecimal price) {}
}
