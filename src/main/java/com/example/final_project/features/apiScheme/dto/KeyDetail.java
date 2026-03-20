package com.example.final_project.features.apiScheme.dto;

public record KeyDetail(
        String columnName,
        boolean primaryKey,
        boolean foreignKey,
        String referenceTable
) {}