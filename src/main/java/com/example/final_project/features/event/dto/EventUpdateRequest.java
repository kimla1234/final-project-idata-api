package com.example.final_project.features.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

public record EventUpdateRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        Date start_date,
        String time,
        Date end_date,

        String image,
        String seat_plan,
        String location_name,
        String latitude,
        String longitude,

        Set<String> imagesEvent,

        Boolean status,

        Integer categoryId,

        // We include tickets in case the user wants to update the ticket list too
        List<TicketTypeUpdateRequest> ticketTypes
) {
    public record TicketTypeUpdateRequest(
            @NotBlank Integer id,
            @NotBlank String name,
            @NotNull Double price,
            @NotNull Integer total_quantity,
            Boolean is_published,
            Boolean is_display
    ) {}
}
