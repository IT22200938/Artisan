package com.artisan.listing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateListingRequest {

    @NotBlank
    private String sellerId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank
    private String category;
    private String country;
    private List<String> imageUrls;

    @DecimalMin("0.01")
    private BigDecimal price;

    private String currency;

    @Min(0)
    private int stockQuantity;
}
