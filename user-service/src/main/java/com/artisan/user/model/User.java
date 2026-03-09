package com.artisan.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private String displayName;
    private String avatarUrl;

    @Indexed
    private UserRole role;  // BUYER or SELLER

    private String country;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;

    public enum UserRole {
        BUYER,
        SELLER
    }
}
