package com.example.final_project.features.woekspace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record WorkspaceInviteRequest(
        @NotBlank @Email String email,
        @NotBlank String role // "ADMIN", "EDITOR", "VIEWER"
) {
}
