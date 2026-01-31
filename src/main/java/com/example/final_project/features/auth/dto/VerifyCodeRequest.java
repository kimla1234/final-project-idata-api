package com.example.final_project.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid", regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        String email,

        @NotBlank(message = "verify code is required")
        String verificationCode
) {
}
