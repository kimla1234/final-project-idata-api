package com.example.final_project.features.woekspace.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkspaceRoleUpdateRequest(@NotBlank(message = "Role is required")
                                         String role) {}