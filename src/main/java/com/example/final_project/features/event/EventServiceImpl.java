package com.example.final_project.features.event;

import com.example.final_project.domain.Event;
import com.example.final_project.domain.EventCategory;
import com.example.final_project.domain.Organizer;
import com.example.final_project.domain.User;
import com.example.final_project.features.event.dto.EventRequest;
import com.example.final_project.features.event.dto.EventResponse;
import com.example.final_project.features.event.dto.EventUpdateRequest;
import com.example.final_project.features.organizer.OrganizerRepository;
import com.example.final_project.features.organizer.OrganizerService;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.mapper.EventMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final EventCategoryRepository categoryRepository;
    private final OrganizerRepository organizerRepository;

    @Override
    public Page<EventResponse> findAllByIsDeletedFalse(int page, int size) {
        // បង្កើត Pageable ពី int page និង size
        Pageable pageable = PageRequest.of(page, size);

        // បញ្ជូន pageable ទៅកាន់ Repository
        return eventRepository.findAllByIsDeletedFalse(pageable)
                .map(eventMapper::toEventResponse);
    }


    @Override
    public EventResponse getEventByUuid(String uuid) {
        Event event = eventRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "find not found UUID: " + uuid
                ));

        return eventMapper.toEventResponse(event);
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest eventRequest, Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = jwt.getClaimAsString("iss");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        boolean isOrganizer = user.getRoles().stream().anyMatch(r -> r.getName().equals("ORGANIZER"));

        // Check roles first
        if (!isAdmin && !isOrganizer) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        Organizer organizer = null;
        if (isOrganizer) {
            organizer = organizerRepository.findByCreatedBy(user)
                    .orElseGet(() -> {
                        // If the role exists but the profile doesn't, create it on the fly
                        Organizer newOrganizer = new Organizer();
                        newOrganizer.setCreatedBy(user);
                        return organizerRepository.save(newOrganizer);
                    });
        }

        // If ADMIN, organizer can be null, or you can assign a default organizer if needed
        // For now, let's assign null

        EventCategory category = categoryRepository.findById(eventRequest.category_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Event event = eventMapper.fromRequest(eventRequest);
        event.setUuid(UUID.randomUUID().toString());
        event.setStatus(true);
        event.setOrganizer(organizer); // null for ADMIN, existing organizer for ORGANIZER
        event.setCategory(category);

        if (event.getTicketTypes() != null) {
            event.getTicketTypes().forEach(ticket -> ticket.setEvent(event));
        }

        eventRepository.save(event);

        return eventMapper.toEventResponse(event);
    }

    @Override
    public List<EventResponse> getMyEvents(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // ១. ទាញយក Email ពី JWT
        String email = jwt.getClaimAsString("iss");

        // ២. រក User ក្នុង Database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ៣. រក Organizer profile របស់ User នេះ
        Organizer organizer = organizerRepository.findByCreatedBy(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer profile not found for this user"));

        // ៤. ទាញយក Events ទាំងអស់ដែលជារបស់ Organizer នេះ
        // អ្នកត្រូវប្រាកដថាមាន method នេះក្នុង EventRepository (មើលជំហានទី ៣)
        List<Event> events = eventRepository.findByOrganizer(organizer);

        return eventMapper.toEventResponseList(events);
    }

    @Override
    public List<EventResponse> getEventsThisWeek() {
        LocalDateTime now = LocalDateTime.now();

        // ចាប់ពី 7 ថ្ងៃមុន
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // ថ្ងៃចុងសប្តាហ៍ (អាទិត្យ) វេលាម៉ោង 23:59:59
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23)
                .withMinute(59)
                .withSecond(59);

        List<Event> events = eventRepository.findEventsForThisWeekByIsDeletedFalse(sevenDaysAgo, endOfWeek);
        return eventMapper.toEventResponseList(events);
    }

    @Override
    public void deleteEventByUuid(String uuid) {
        Event event = eventRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));

        event.setIsDeleted(true);   // ✅ Soft delete
        event.setStatus(false);     // (optional) disable event

        eventRepository.save(event); // ✅ save change
    }


    @Override
    public List<EventResponse> filterEvents(String category, String title) {

        // If param is null, pass empty string so LIKE '%%' matches everything
        String categoryParam = (category == null) ? "" : category;
        String titleParam = (title == null) ? "" : title;

        List<Event> events = eventRepository.findByFilter(categoryParam, titleParam);
        return eventMapper.toEventResponseList(events);
    }

    @Override
    public EventResponse updateEvent(String uuid, @Valid EventUpdateRequest eventUpdateRequest) {
        // 1. ទាញយក Event ដែលមានស្រាប់
        Event existingEvent = eventRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        // 2. Map ទិន្នន័យថ្មីចូល (Title, Description, etc.)
        eventMapper.updateEventFromRequest(eventUpdateRequest, existingEvent);

        // 3. Update រូបភាព List (សំខាន់៖ ប្រើ () ព្រោះវាជា Record)
        if (eventUpdateRequest.imagesEvent() != null) {
            existingEvent.setImagesEvent(eventUpdateRequest.imagesEvent());
        }

        // 4. កំណត់ Link សំបុត្រ (Tickets) ឡើងវិញ
        if (existingEvent.getTicketTypes() != null) {
            existingEvent.getTicketTypes().forEach(ticket -> ticket.setEvent(existingEvent));
        }

        return eventMapper.toEventResponse(eventRepository.save(existingEvent));
    }
}
