package com.artisan.order.client.dto;

import java.math.BigDecimal;

public record StockCheckResponse(boolean available, int requestedQuantity, int availableStock, BigDecimal unitPrice) {}
