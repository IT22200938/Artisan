package com.artisan.user.controller;

import com.artisan.user.dto.UpdateProfileRequest;
import com.artisan.user.dto.UserProfileResponse;
import com.artisan.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile and validation endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID (for inter-service integration)")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String id) {
        UserProfileResponse profile = userService.getProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update own profile (requires authentication)")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String currentUserId = (String) authentication.getPrincipal();
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }
        UserProfileResponse profile = userService.updateProfile(id, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}/validate")
    @Operation(summary = "Validate user exists (internal - for Order/Review service integration)")
    public ResponseEntity<ValidationResponse> validateUser(@PathVariable String id) {
        boolean valid = userService.validateUser(id);
        return ResponseEntity.ok(new ValidationResponse(id, valid));
    }

    public record ValidationResponse(String userId, boolean valid) {}
}
