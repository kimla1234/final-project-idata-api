package com.example.final_project.features.auth.dto;

import lombok.Builder;

@Builder
public record ResetTokenResponse(
        String email,

        String resetToken,

        String verifyCode,

        String message
) {
}
