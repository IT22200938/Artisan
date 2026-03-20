package com.artisan.order.controller;

import com.artisan.order.dto.AddToCartRequest;
import com.artisan.order.dto.OrderResponse;
import com.artisan.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Cart and checkout")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/cart")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<Void> addToCart(
            @RequestHeader("X-Buyer-Id") String buyerId,
            @Valid @RequestBody AddToCartRequest request) {
        orderService.addToCart(buyerId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout (mock payment)")
    public ResponseEntity<OrderResponse> checkout(
            @RequestHeader("X-Buyer-Id") String buyerId) {
        OrderResponse order = orderService.checkout(buyerId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "List orders for buyer")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestHeader("X-Buyer-Id") String buyerId) {
        return ResponseEntity.ok(orderService.getOrdersByBuyer(buyerId));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List orders containing items sold by a seller")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(@PathVariable String sellerId) {
        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }
}
