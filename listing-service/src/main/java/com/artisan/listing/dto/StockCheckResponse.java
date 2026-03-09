package com.artisan.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {
    private boolean available;
    private int requestedQuantity;
    private int availableStock;
    private BigDecimal unitPrice;
}
