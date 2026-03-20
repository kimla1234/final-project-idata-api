package com.example.final_project.features.apiScheme.dto;

import java.util.List;

public record ApiGenerationRequest(
        String schemaName,
        String description,
        List<PropertyDetail> properties,
        List<KeyDetail> keys
) {}
