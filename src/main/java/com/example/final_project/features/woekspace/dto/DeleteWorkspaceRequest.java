package com.example.final_project.features.woekspace.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteWorkspaceRequest(
        @NotBlank String password
) {}