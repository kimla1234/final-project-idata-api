package com.example.final_project.features.organizer.dto;

public record OrganizerResponse(
        Integer id,
        String name,
        String email,
        String phone,
        String logoImage,
        String description,
        String address,
        Boolean status,
        String createdByUserUuid
) {
}
