package com.artisan.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String id;
    private String listingId;
    private String orderId;
    private String userId;
    private String userDisplayName;
    private String userAvatarUrl;
    private int rating;
    private String comment;
    private Instant createdAt;
}
