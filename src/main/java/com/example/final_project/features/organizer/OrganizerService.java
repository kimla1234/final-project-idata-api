package com.example.final_project.features.organizer;

import com.example.final_project.features.organizer.dto.OrganizerCreateRequest;
import com.example.final_project.features.organizer.dto.OrganizerResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface OrganizerService {
    OrganizerResponse createOrganizer(OrganizerCreateRequest request, Jwt jwt);
    Optional<OrganizerResponse> getOrganizerById(Integer organizerId);
    OrganizerResponse getMyOrganizer(Jwt jwt);

}
