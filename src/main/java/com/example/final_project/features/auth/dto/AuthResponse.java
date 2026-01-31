package com.example.final_project.features.auth.dto;

import com.example.final_project.features.user.dto.UserResponse;

public record AuthResponse(
        String type,
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
