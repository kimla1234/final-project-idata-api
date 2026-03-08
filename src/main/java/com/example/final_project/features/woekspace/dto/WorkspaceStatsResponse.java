package com.example.final_project.features.woekspace.dto;

public record WorkspaceStatsResponse(
        long totalCampaigns,
        long totalEmailsSent,
        long totalSuccess,
        long totalFailure,
        double overallSuccessRate
) {}
