package com.example.final_project.features.folder.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
        @NotBlank String name
) {
}
