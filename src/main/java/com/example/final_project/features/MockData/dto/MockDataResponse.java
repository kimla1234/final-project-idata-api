package com.example.final_project.features.MockData.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record MockDataResponse(
        Integer id,
        Map<String, Object> data,
        Integer apiSchemeId,
        LocalDateTime createdAt
) {}