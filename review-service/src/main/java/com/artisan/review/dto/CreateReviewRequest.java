package com.artisan.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotBlank
    private String listingId;

    @NotBlank
    private String orderId;

    @NotBlank
    private String userId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 1000)
    private String comment;
}
