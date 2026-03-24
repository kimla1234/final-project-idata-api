package com.example.final_project.mapper;

import com.example.final_project.domain.Folder;
import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FolderMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workspace", ignore = true)
    Folder fromRequest(FolderRequest request);

    FolderResponse toResponse(Folder folder);

    List<FolderResponse> toResponseList(List<Folder> folders);
}
