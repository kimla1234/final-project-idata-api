package com.example.final_project.features.organizer;

import com.example.final_project.domain.Organizer;
import com.example.final_project.domain.Role;
import com.example.final_project.domain.User;
import com.example.final_project.features.organizer.dto.OrganizerCreateRequest;
import com.example.final_project.features.organizer.dto.OrganizerResponse;
import com.example.final_project.features.user.RoleRepository;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.mapper.OrganizerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizerServiceImpl implements OrganizerService  {

    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;
    private final OrganizerMapper organizerMapper;
    private final RoleRepository roleRepository;


    @Override
    public OrganizerResponse createOrganizer(OrganizerCreateRequest request, Jwt jwt) {

        String email = jwt.getClaimAsString("iss");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Role organizerRole = roleRepository.findByName("ORGANIZER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role ORGANIZER not found"));

// Add role if user doesn't already have it
        if (!user.getRoles().contains(organizerRole)) {
            user.getRoles().add(organizerRole);
            userRepository.save(user);  // save user with new role
        }

        if (organizerRepository.existsByCreatedBy(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organizer already exists");
        }

        Organizer organizer = organizerMapper.toEntity(request);
        organizer.setCreatedBy(user);

        organizerRepository.save(organizer);

        return organizerMapper.toResponse(organizer);
    }

    @Override
    public Optional<OrganizerResponse> getOrganizerById(Integer organizerId) {
        Integer id;
        try {
            id = Integer.valueOf(organizerId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return organizerRepository.findById(id)
                .map(organizerMapper::toResponse);
    }

    @Override
    public OrganizerResponse getMyOrganizer(Jwt jwt) {

        String email = jwt.getClaimAsString("sub");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Organizer organizer = organizerRepository.findByCreatedBy(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer not found"));

        return organizerMapper.toResponse(organizer);
    }
}

