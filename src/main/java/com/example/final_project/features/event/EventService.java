package com.example.final_project.features.event;

import com.example.final_project.features.event.dto.EventRequest;
import com.example.final_project.features.event.dto.EventResponse;
import com.example.final_project.features.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EventService {
    // Change List to Page and add Pageable parameter
    Page<EventResponse> findAllByIsDeletedFalse(int page ,  int size);
    EventResponse getEventByUuid(String uuid);
    EventResponse createEvent(EventRequest eventRequest , Jwt jwt);
    List<EventResponse> getMyEvents(Jwt jwt);
    List<EventResponse> getEventsThisWeek();

    void deleteEventByUuid(String uuid);
    // Dynamic filter method
    List<EventResponse> filterEvents(String category, String title);
    EventResponse updateEvent(String uuid, @Valid EventUpdateRequest eventUpdateRequest);

}
