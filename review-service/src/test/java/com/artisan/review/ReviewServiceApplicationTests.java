package com.artisan.review;

import com.artisan.review.client.UserServiceClient;
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
class ReviewServiceApplicationTests {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6");

    @MockBean
    UserServiceClient userServiceClient;

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
        registry.add("integration.user-service.url", () -> "http://localhost:8080");
    }

    @Test
    void contextLoads() {
    }
}
