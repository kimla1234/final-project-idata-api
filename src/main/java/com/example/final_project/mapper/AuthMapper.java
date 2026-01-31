package com.example.final_project.mapper;

import com.example.final_project.domain.User;
import com.example.final_project.features.auth.dto.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User mapFromRegisterCreateRequest(RegisterRequest registerRequest);

}
