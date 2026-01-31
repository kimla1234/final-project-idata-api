package com.example.final_project.mapper;

import com.example.final_project.domain.Organizer;
import com.example.final_project.features.organizer.dto.OrganizerCreateRequest;
import com.example.final_project.features.organizer.dto.OrganizerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizerMapper {

    // Request DTO → Entity
    Organizer toEntity(OrganizerCreateRequest  organizerCreateRequest);

    // Entity → Response DTO
    @Mapping(source = "createdBy.uuid", target = "createdByUserUuid")
    OrganizerResponse toResponse(Organizer organizer);


}
