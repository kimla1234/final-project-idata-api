package com.example.final_project.features.apiScheme.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiSchemeStatsResponse(
        Integer apiSchemeId,
        String apiName,

        Integer totalRequests,
        Integer totalForks,
        Integer totalViews,


        LocalDateTime lastAccessedAt,
        String topReferrer,

        Double popularityScore,


        Map<String, Integer> usageHistory
) {}