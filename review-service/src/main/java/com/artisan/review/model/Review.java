package com.artisan.review.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String listingId;

    @Indexed
    private String orderId;

    @Indexed
    private String userId;

    private int rating;  // 1-5
    private String comment;

    private Instant createdAt;

    @Indexed
    private boolean visible;
}
