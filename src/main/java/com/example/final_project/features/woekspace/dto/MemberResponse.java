package com.example.final_project.features.woekspace.dto;

public record MemberResponse(
        Integer id,
        String username,
        String email,
        String role,
        String profileImage // ប្រសិនបើមាន
) {}
