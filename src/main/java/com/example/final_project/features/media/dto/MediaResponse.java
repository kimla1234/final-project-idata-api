package com.example.final_project.features.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record MediaResponse(
        String name,

        String contentType,

        String uri,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size,

        String extension
) {
}
