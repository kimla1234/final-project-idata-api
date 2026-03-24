package com.example.final_project.mapper;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.domain.Role;
import com.example.final_project.features.apiScheme.dto.ApiSchemeCreateRequest;
import com.example.final_project.features.apiScheme.dto.ApiSchemeResponse;
import com.example.final_project.features.apiScheme.dto.ApiSchemeUpdateRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiSchemeMapper {

    default String mapRoleToString(Role role) {
        if (role == null) return null;
        return role.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "folder", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "parentApi", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    ApiScheme fromCreateRequest(ApiSchemeCreateRequest request);

    @Mapping(source = "apiKey", target = "apiKey")
    @Mapping(source = "folder.id", target = "folderId")
    @Mapping(source = "folder.name", target = "folderName")
    @Mapping(source = "workspace.id", target = "workspaceId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(source = "owner.profileImage", target = "ownerAvatar")
    @Mapping(target = "ownerHandle", expression = "java((apiScheme.getOwner().getName()))")
    @Mapping(source = "owner.uuid", target = "ownerUuid")
    @Mapping(source = "parentApi.id", target = "parentId")
    @Mapping(source = "parentApi.name", target = "parentName")
    @Mapping(target = "isFollowed", ignore = true)
    ApiSchemeResponse toResponse(ApiScheme apiScheme);



    List<ApiSchemeResponse> toResponseList(List<ApiScheme> apiSchemes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "endpointUrl", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(ApiSchemeUpdateRequest request, @MappingTarget ApiScheme scheme);
}