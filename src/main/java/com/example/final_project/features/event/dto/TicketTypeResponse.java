package com.example.final_project.features.event.dto;

public record TicketTypeResponse(
        Integer id,
        String name,
        Double price,
        Integer total_quantity,
        Integer sold_quantity
) {}