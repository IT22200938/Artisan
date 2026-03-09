package com.artisan.listing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockReduceRequest {
    @NotBlank
    private String listingId;

    @NotNull
    @Min(1)
    private int quantity;
}
