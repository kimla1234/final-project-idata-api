package com.example.final_project.features.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        String uuid,

        String name,

        String email,

        LocalDate dob,

        String phone,
        String address,

        String profileImage,

        String coverImage,
        Integer followersCount,
        List<String> roles,

        Boolean isBlock,

        Boolean isDelete,

        LocalDateTime createdAt,

        LocalDateTime lastModifiedAt
) {
}
