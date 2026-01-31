package com.example.final_project.features.event.dto;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record EventResponse(
        Integer id,
        String uuid,
        String title,
        String description,
        Date start_date,
        Date end_date,
        String image,
        String start_time,
        String end_time,
        List<String> imagesEvent,
        String latitude,
        String longitude,
        String location_name,
        String categoryName,
        List<TicketTypeResponse> ticketTypes,
        LocalDateTime createdAt,
        Boolean status,
        Boolean isDeleted,
        Integer organizerId

) {
    // បង្កើត inner record សម្រាប់បង្ហាញ TicketType ក្នុង Event
    public record TicketTypeResponse(
            Integer id,
            String name,
            Double price,
            Integer total_quantity,
            Integer sold_quantity
    ) {}
}

