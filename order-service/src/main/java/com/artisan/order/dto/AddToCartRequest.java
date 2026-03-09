package com.artisan.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotBlank
    private String listingId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
