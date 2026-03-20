package com.artisan.order.service;

import com.artisan.order.client.ListingServiceClient;
import com.artisan.order.client.UserServiceClient;
import com.artisan.order.dto.AddToCartRequest;
import com.artisan.order.dto.OrderResponse;
import com.artisan.order.model.Cart;
import com.artisan.order.model.Order;
import com.artisan.order.repository.CartRepository;
import com.artisan.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ListingServiceClient listingServiceClient;

    public void addToCart(String buyerId, AddToCartRequest request) {
        if (!userServiceClient.validateUser(buyerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user: " + buyerId);
        }

        var stockCheck = listingServiceClient.checkStock(request.getListingId(), request.getQuantity());
        if (!stockCheck.available()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for listing " + request.getListingId());
        }

        var listingInfo = listingServiceClient.getListing(request.getListingId());

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setId(UUID.randomUUID().toString());
                    c.setBuyerId(buyerId);
                    c.setItems(new ArrayList<>());
                    return c;
                });

        var existing = cart.getItems().stream()
                .filter(i -> i.getListingId().equals(request.getListingId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(new Cart.CartItem(
                    request.getListingId(),
                    listingInfo.title(),
                    request.getQuantity(),
                    listingInfo.price()
            ));
        }

        cartRepository.save(cart);
    }

    @Transactional
    public OrderResponse checkout(String buyerId) {
        if (!userServiceClient.validateUser(buyerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user: " + buyerId);
        }

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        List<Order.OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String currency = "USD";

        for (var item : cart.getItems()) {
            var stockCheck = listingServiceClient.checkStock(item.getListingId(), item.getQuantity());
            if (!stockCheck.available()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for listing " + item.getListingId());
            }
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
            orderItems.add(new Order.OrderItem(
                    item.getListingId(),
                    item.getTitle(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    lineTotal
            ));
        }

        for (var item : orderItems) {
            listingServiceClient.reduceStock(item.getListingId(), item.getQuantity());
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(buyerId)
                .status(Order.OrderStatus.PAID)
                .items(orderItems)
                .totalAmount(total)
                .currency(currency)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        order = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByBuyer(String buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersBySeller(String sellerId) {
        Map<String, String> listingSellerCache = new HashMap<>();

        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(order -> toSellerOrderResponse(order, sellerId, listingSellerCache))
                .filter(response -> !response.getItems().isEmpty())
                .toList();
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        var items = order.getItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(
                        i.getListingId(),
                        i.getTitle(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                ))
                .toList();
        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .status(order.getStatus().name())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderResponse toSellerOrderResponse(Order order, String sellerId, Map<String, String> listingSellerCache) {
        var sellerItems = order.getItems().stream()
                .filter(item -> isSellerListing(item.getListingId(), sellerId, listingSellerCache))
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getListingId(),
                        item.getTitle(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()
                ))
                .toList();

        BigDecimal sellerTotal = sellerItems.stream()
                .map(OrderResponse.OrderItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .status(order.getStatus().name())
                .items(sellerItems)
                .totalAmount(sellerTotal)
                .currency(order.getCurrency())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private boolean isSellerListing(String listingId, String sellerId, Map<String, String> listingSellerCache) {
        String ownerId = listingSellerCache.computeIfAbsent(listingId, id -> listingServiceClient.getListing(id).sellerId());
        return sellerId.equals(ownerId);
    }
}
