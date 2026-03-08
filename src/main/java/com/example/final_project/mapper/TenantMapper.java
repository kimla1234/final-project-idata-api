package com.example.final_project.mapper;

import com.example.final_project.features.tennat.dto.TenantRequest;
import com.example.final_project.features.tennat.dto.TenantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "folder", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    Tenant fromRequest(TenantRequest request);

    @Mapping(target = "folderId", source = "folder.id")
    @Mapping(target = "folderName", source = "folder.name")
    TenantResponse toResponse(Tenant tenant);

    List<TenantResponse> toResponseList(List<Tenant> tenants);
}