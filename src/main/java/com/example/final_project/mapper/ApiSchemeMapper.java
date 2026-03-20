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

    // ១. បន្ថែម Method នេះដើម្បីបំប្លែង List<Role> ទៅជា List<String> ក្នុង User Object
    default String mapRoleToString(Role role) {
        if (role == null) return null;
        return role.getName(); // ប្រើឈ្មោះ field ឱ្យត្រូវតាម Role Entity របស់បង
    }

    // ២. បំប្លែងពី Request ទៅជា Entity (សម្រាប់ Create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "folder", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "parentApi", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true) // បន្ថែមសម្រាប់ Audit
    ApiScheme fromCreateRequest(ApiSchemeCreateRequest request);

    // ៣. បំប្លែងពី Entity ទៅជា Response DTO
    @Mapping(source = "apiKey", target = "apiKey")
    @Mapping(source = "folder.id", target = "folderId")
    @Mapping(source = "folder.name", target = "folderName")
    @Mapping(source = "workspace.id", target = "workspaceId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(source = "owner.profileImage", target = "ownerAvatar") // 🎯 ទាញពី field profileImage
    @Mapping(target = "ownerHandle", expression = "java((apiScheme.getOwner().getName()))")
    @Mapping(source = "owner.uuid", target = "ownerUuid")
    @Mapping(source = "parentApi.id", target = "parentId")
    @Mapping(source = "parentApi.name", target = "parentName")
    @Mapping(target = "isFollowed", ignore = true)
    // MapStruct នឹងប្រើ mapRoleToString អូតូសម្រាប់ nested objects (Owner/LastModifiedBy)
    ApiSchemeResponse toResponse(ApiScheme apiScheme);



    List<ApiSchemeResponse> toResponseList(List<ApiScheme> apiSchemes);

    // ៤. Update Entity ដែលមានស្រាប់
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    @Mapping(target = "endpointUrl", ignore = true) // យើង handle endpointUrl manual ក្នុង service
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(ApiSchemeUpdateRequest request, @MappingTarget ApiScheme scheme);
}