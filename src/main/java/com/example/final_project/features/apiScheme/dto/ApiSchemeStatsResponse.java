package com.example.final_project.features.apiScheme.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiSchemeStatsResponse(
        Integer apiSchemeId,
        String apiName,

        // ស្ថិតិសរុប (Cumulative Stats)
        Integer totalRequests,  // ចំនួន Call API សរុប
        Integer totalForks,     // ចំនួនដែលគេចម្លងយកទៅប្រើ
        Integer totalViews,     // ចំនួនអ្នកចូលមើលក្នុង Community

        // ព័ត៌មានអំពីសកម្មភាពចុងក្រោយ
        LocalDateTime lastAccessedAt,
        String topReferrer,     // ប្រភពដែល Call ច្រើនជាងគេ (ឧទាហរណ៍: localhost, mobile-app)

        /**
         * កម្រិតនៃភាពពេញនិយម (Popularity Score)
         * អាចគណនាដោយ (Forks * 2) + (Views * 0.5) + Requests
         */
        Double popularityScore,

        // ស្ថិតិប្រចាំថ្ងៃ ឬប្រចាំសប្តាហ៍ (Optional: សម្រាប់ធ្វើ Graph)
        // ឧទាហរណ៍: {"2026-03-01": 50, "2026-03-02": 120}
        Map<String, Integer> usageHistory
) {}