package com.artisan.review.repository;

import com.artisan.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByListingIdAndVisibleTrue(String listingId, Pageable pageable);

    Optional<Review> findByOrderIdAndUserId(String orderId, String userId);
}
