package com.example.final_project.features.organizer.dto;

import jakarta.validation.constraints.NotBlank;

public record OrganizerCreateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        String email,

        String phone,
        String logoImage,
        String description,
        String address
) {
}
