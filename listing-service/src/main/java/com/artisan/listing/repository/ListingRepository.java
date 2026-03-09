package com.artisan.listing.repository;

import com.artisan.listing.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ListingRepository extends MongoRepository<Listing, String> {

    Page<Listing> findByActiveTrue(Pageable pageable);

    Page<Listing> findByCategoryAndActiveTrue(String category, Pageable pageable);

    Page<Listing> findBySellerIdAndActiveTrue(String sellerId, Pageable pageable);

    @Query("{ $or: [ { title: { $regex: ?0, $options: 'i' } }, { description: { $regex: ?0, $options: 'i' } } ], active: true }")
    List<Listing> searchByText(String searchTerm, Pageable pageable);
}
