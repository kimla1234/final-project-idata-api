package com.example.final_project.features.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

public record EventRequest(
        @NotBlank(message = "ចំណងជើងមិនអាចទទេបានទេ")
        String title,

        String description,

        @NotNull(message = "ថ្ងៃចាប់ផ្ដើមមិនអាចទទេបានទេ")
        Date start_date,

        @NotNull(message = "not null")
        String start_time,
        @NotNull(message = "not null")
        String end_time,

        @NotNull(message = "ថ្ងៃបញ្ចប់មិនអាចទទេបានទេ")
        Date end_date,

        String image,
        String seat_plan,
        String location_name,
        String latitude,
        String longitude,

        @NotNull(message = "សូមជ្រើសរើសប្រភេទកម្មវិធី (Category)")
        Integer category_id, // ផ្ញើត្រឹម ID នៃ Category មកបានហើយ

        List<TicketTypeRequest> ticketTypes // បញ្ជីប្រភេទសំបុត្រដែលចង់បង្កើតជាមួយ Event
) {
    // Inner record សម្រាប់ទទួលទិន្នន័យ TicketType
    public record TicketTypeRequest(
            Integer id,
            @NotBlank String name,
            @NotNull Double price,
            @NotNull Integer total_quantity,
            Boolean is_published,
            Boolean is_display
    ) {}
}
