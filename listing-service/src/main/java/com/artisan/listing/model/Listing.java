package com.artisan.listing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "listings")
public class Listing {

    @Id
    private String id;

    @Indexed
    private String sellerId;

    private String title;
    private String description;

    private String category;  // jewelry, textiles, pottery, etc.
    private String country;
    private List<String> imageUrls;

    private BigDecimal price;
    private String currency;

    @Indexed
    private int stockQuantity;

    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
