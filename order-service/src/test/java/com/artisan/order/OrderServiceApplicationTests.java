package com.artisan.order;

import com.artisan.order.client.ListingServiceClient;
import com.artisan.order.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
class OrderServiceApplicationTests {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @MockBean
    UserServiceClient userServiceClient;

    @MockBean
    ListingServiceClient listingServiceClient;

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
        registry.add("integration.user-service.url", () -> "http://localhost:8080");
        registry.add("integration.listing-service.url", () -> "http://localhost:8081");
    }

    @Test
    void contextLoads() {
    }
}
