package com.example.final_project.features.event;

import com.example.final_project.domain.Event;
import com.example.final_project.features.event.dto.EventRequest;
import com.example.final_project.features.event.dto.EventResponse;
import com.example.final_project.features.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;


    @GetMapping("/all")
    public Page<EventResponse> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // បង្កើត Pageable object ពី parameter ដែលទទួលមក
        Pageable pageable = PageRequest.of(page, size);

        return eventService.findAllByIsDeletedFalse(page,size);
    }




    @GetMapping("/this-week")
    public List<EventResponse> getEventsThisWeek() {
        return eventService.getEventsThisWeek();
    }

    @GetMapping("/{uuid}")
    public EventResponse getEventByUuid(@PathVariable String uuid) {
        return eventService.getEventByUuid(uuid);
    }

    @GetMapping
    public List<EventResponse> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title
           ) {

        return eventService.filterEvents(category, title);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse addEvent(
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return eventService.createEvent(eventRequest, jwt);
    }


    @PatchMapping ("/{uuid}")
    public EventResponse updateEvent(@PathVariable String uuid, @Valid @RequestBody EventUpdateRequest eventRequest) {
        return eventService.updateEvent(uuid ,eventRequest);
    }


    @GetMapping("/my-events")
    public List<EventResponse> getMyEvents(@AuthenticationPrincipal Jwt jwt) {
        return eventService.getMyEvents(jwt);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable String uuid) {
        eventService.deleteEventByUuid(uuid);
    }


}
