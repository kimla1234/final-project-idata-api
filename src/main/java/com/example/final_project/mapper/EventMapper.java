package com.example.final_project.mapper;

import com.example.final_project.domain.Event;
import com.example.final_project.features.event.dto.EventRequest;
import com.example.final_project.features.event.dto.EventResponse;
import com.example.final_project.features.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    /**
     * Convert Event Entity to EventResponse DTO.
     * Maps the 'name' field from the Category object to 'categoryName' in the response.
     */
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "organizer.id", target = "organizerId") // ✅ Add this line
    EventResponse toEventResponse(Event event);

    /**
     * Map a list of Event Entities to a list of EventResponse DTOs.
     */
    List<EventResponse> toEventResponseList(List<Event> events);

    /**
     * Convert EventRequest DTO to Event Entity for saving.
     * Maps 'categoryId' from the request to the 'id' of the Category object in the entity.
     */
    // ===== Request -> Entity =====
    @Mapping(target = "category", ignore = true) // ✅ IMPORTANT
    Event fromRequest(EventRequest request);

    // Inside EventMapper.java
    @Mapping(target = "category", ignore = true)
    void updateEventFromRequest(@Valid EventUpdateRequest request, @MappingTarget Event event);
}
