package com.example.final_project.mapper;

import com.example.final_project.domain.Folder;
import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FolderMapper {

    // បំប្លែងពី Request DTO ទៅជា Entity (ប្រើពេល Create/Update)
    // យើង ignore id ព្រោះវាជា Auto-generate
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    Folder fromRequest(FolderRequest request);

    // បំប្លែងពី Entity ទៅជា Response DTO (ប្រើពេល Return ទៅ Frontend)
    FolderResponse toResponse(Folder folder);

    // បំប្លែងជា List (សម្រាប់ Get All Folders)
    List<FolderResponse> toResponseList(List<Folder> folders);
}
