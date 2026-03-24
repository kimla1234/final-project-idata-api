package com.example.final_project.features.apiScheme.dto;

import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;
public record ApiSchemeUpdateRequest(
        String name,

        @Pattern(regexp = "^[a-z0-9-]+$", message = "URL អាចប្រើបានតែអក្សរតូច លេខ និងសញ្ញាដក (-) ប៉ុណ្ណោះ")
        String endpointUrl,

        String description,

        List<Map<String, Object>> properties,

        List<Map<String, Object>> keys,

        Boolean isPublic,
        Boolean isPublished,

        Integer folderId
) {}