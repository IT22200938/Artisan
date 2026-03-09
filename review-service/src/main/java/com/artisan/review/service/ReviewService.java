package com.artisan.review.service;

import com.artisan.review.client.UserServiceClient;
import com.artisan.review.dto.CreateReviewRequest;
import com.artisan.review.dto.ReviewResponse;
import com.artisan.review.model.Review;
import com.artisan.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;
    private final UserServiceClient userServiceClient;

    public ReviewResponse create(CreateReviewRequest request) {
        var existing = repository.findByOrderIdAndUserId(request.getOrderId(), request.getUserId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Review already exists for this order");
        }

        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .listingId(request.getListingId())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment() : "")
                .createdAt(Instant.now())
                .visible(true)
                .build();
        review = repository.save(review);

        var userProfile = userServiceClient.getUserProfile(review.getUserId());
        return toResponse(review, userProfile.displayName(), userProfile.avatarUrl());
    }

    public List<ReviewResponse> getByListing(String listingId, int page, int size) {
        return repository.findByListingIdAndVisibleTrue(listingId, PageRequest.of(page, size))
                .stream()
                .map(r -> {
                    var profile = userServiceClient.getUserProfile(r.getUserId());
                    return toResponse(r, profile.displayName(), profile.avatarUrl());
                })
                .collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review review, String displayName, String avatarUrl) {
        return ReviewResponse.builder()
                .id(review.getId())
                .listingId(review.getListingId())
                .orderId(review.getOrderId())
                .userId(review.getUserId())
                .userDisplayName(displayName)
                .userAvatarUrl(avatarUrl)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
