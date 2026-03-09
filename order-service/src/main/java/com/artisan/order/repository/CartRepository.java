package com.artisan.order.repository;

import com.artisan.order.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByBuyerId(String buyerId);
}
