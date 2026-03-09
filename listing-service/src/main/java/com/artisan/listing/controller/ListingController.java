package com.artisan.listing.controller;

import com.artisan.listing.dto.CreateListingRequest;
import com.artisan.listing.dto.ListingResponse;
import com.artisan.listing.dto.StockCheckRequest;
import com.artisan.listing.dto.StockCheckResponse;
import com.artisan.listing.dto.StockReduceRequest;
import com.artisan.listing.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Product listings and search")
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @Operation(summary = "Create a new listing")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody CreateListingRequest request) {
        ListingResponse response = listingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing by ID")
    public ResponseEntity<ListingResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(listingService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all active listings")
    public ResponseEntity<List<ListingResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listingService.list(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search listings by text")
    public ResponseEntity<List<ListingResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listingService.search(q, page, size));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "List by category")
    public ResponseEntity<List<ListingResponse>> byCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listingService.byCategory(category, page, size));
    }

    @PostMapping("/stock/check")
    @Operation(summary = "Check stock availability (for Order Service integration)")
    public ResponseEntity<StockCheckResponse> checkStock(@Valid @RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(listingService.checkStock(request.getListingId(), request.getQuantity()));
    }

    @PostMapping("/stock/reduce")
    @Operation(summary = "Reduce stock (for Order Service integration)")
    public ResponseEntity<Void> reduceStock(@Valid @RequestBody StockReduceRequest request) {
        listingService.reduceStock(request.getListingId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }
}
