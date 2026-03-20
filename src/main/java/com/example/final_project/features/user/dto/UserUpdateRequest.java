package com.example.final_project.features.user.dto;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record UserUpdateRequest(

        String name,

        @Email(message = "Email is invalid", regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        String email,

        LocalDate dob,

        String profileImage,
        String coverImage,

        String phone,

        String address



) {
}
