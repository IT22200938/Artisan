package com.artisan.user.service;

import com.artisan.user.dto.UpdateProfileRequest;
import com.artisan.user.dto.UserProfileResponse;
import com.artisan.user.model.User;
import com.artisan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        return toProfileResponse(user);
    }

    /**
     * Internal validation endpoint for inter-service communication.
     * Used by Order Service, Review Service, etc. to verify user exists.
     */
    public boolean validateUser(String userId) {
        return userRepository.existsById(userId);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .country(user.getCountry())
                .build();
    }
}
