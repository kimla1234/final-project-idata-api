package com.example.final_project.features.woekspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceUpdateRequest(
        @NotBlank(message = "Workspace name is required")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String name,

        @Size(max = 255, message = "Description must be less than 255 characters")
        String description
) {
}
