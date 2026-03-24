package com.example.final_project.features.apiScheme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;

public record ApiSchemeCreateRequest(
        @NotBlank(message = "ឈ្មោះ API មិនអាចទទេបានឡើយ")
        String name,


        @NotBlank(message = "Endpoint URL មិនអាចទទេបានឡើយ")
        @Pattern(regexp = "^[a-z0-9/-]+$", message = "URL អាចប្រើបានតែអក្សរតូច លេខ សញ្ញាដក (-) និងសញ្ញា (/) ប៉ុណ្ណោះ")
        String endpointUrl,

        String description,

        @NotNull(message = "រចនាសម្ព័ន្ធ API (Properties) ត្រូវតែមាន")
        List<Map<String, Object>> properties,



        List<Map<String, Object>> keys,

        Boolean isPublic,

        @NotNull(message = "សូមជ្រើសរើស Folder សម្រាប់ទុក API នេះ")
        Integer folderId
) {}