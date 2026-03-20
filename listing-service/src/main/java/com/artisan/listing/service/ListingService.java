package com.artisan.listing.service;

import com.artisan.listing.dto.CreateListingRequest;
import com.artisan.listing.dto.ListingResponse;
import com.artisan.listing.dto.StockCheckResponse;
import com.artisan.listing.model.Listing;
import com.artisan.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository repository;

    public ListingResponse create(CreateListingRequest request) {
        Listing listing = Listing.builder()
                .id(UUID.randomUUID().toString())
                .sellerId(request.getSellerId())
                .title(request.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : "")
                .category(request.getCategory())
                .country(request.getCountry())
                .imageUrls(request.getImageUrls())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .stockQuantity(request.getStockQuantity())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        listing = repository.save(listing);
        return toResponse(listing);
    }

    public ListingResponse getById(String id) {
        Listing listing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found: " + id));
        return toResponse(listing);
    }

    public List<ListingResponse> list(int page, int size) {
        return repository.findByActiveTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ListingResponse> search(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return list(page, size);
        }
        return repository.searchByText(query, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ListingResponse> byCategory(String category, int page, int size) {
        return repository.findByCategoryAndActiveTrue(category, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ListingResponse> bySeller(String sellerId, int page, int size) {
        return repository.findBySellerId(sellerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Stock check - for Order Service integration
     */
    public StockCheckResponse checkStock(String listingId, int quantity) {
        Listing listing = repository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found: " + listingId));
        boolean available = listing.getStockQuantity() >= quantity && listing.isActive();
        return StockCheckResponse.builder()
                .available(available)
                .requestedQuantity(quantity)
                .availableStock(listing.getStockQuantity())
                .unitPrice(listing.getPrice())
                .build();
    }

    @Transactional
    public void reduceStock(String listingId, int quantity) {
        Listing listing = repository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found: " + listingId));
        if (listing.getStockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for listing: " + listingId);
        }
        listing.setStockQuantity(listing.getStockQuantity() - quantity);
        listing.setUpdatedAt(Instant.now());
        repository.save(listing);
    }

    private ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .sellerId(listing.getSellerId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .category(listing.getCategory())
                .country(listing.getCountry())
                .imageUrls(listing.getImageUrls())
                .price(listing.getPrice())
                .currency(listing.getCurrency())
                .stockQuantity(listing.getStockQuantity())
                .active(listing.isActive())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}
