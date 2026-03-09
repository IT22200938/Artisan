package com.artisan.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100)
    private String displayName;
    private String avatarUrl;
    @Size(max = 100)
    private String country;
}
