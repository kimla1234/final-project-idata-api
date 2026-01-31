package com.example.final_project.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "email is required")
        String newPassword,

        @NotBlank(message = "password is required")
        String confirmPassword,

        @NotBlank(message = "password is required")
        String resetToken
) {
}
