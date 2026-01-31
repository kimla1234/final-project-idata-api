package com.example.final_project.features.organizer;

import com.example.final_project.features.organizer.dto.OrganizerCreateRequest;
import com.example.final_project.features.organizer.dto.OrganizerResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/organizer")
@AllArgsConstructor
public class OrganizerController {
    private final OrganizerService organizerService;

    @PostMapping("/create")
    public OrganizerResponse createOrganizer(
            @RequestBody @Valid OrganizerCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return organizerService.createOrganizer(request, jwt);
    }

    @RequestMapping("/my-organizer")
    public OrganizerResponse getMyOrganizer(@AuthenticationPrincipal Jwt jwt){
        return organizerService.getMyOrganizer(jwt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponse> getOrganizer(@PathVariable("id") String id) {
        return organizerService.getOrganizerById(Integer.valueOf(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
