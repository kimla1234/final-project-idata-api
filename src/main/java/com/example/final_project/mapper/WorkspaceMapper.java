package com.example.final_project.mapper;

import com.example.final_project.domain.Workspace;
import com.example.final_project.features.woekspace.dto.WorkspaceCreateRequest;
import com.example.final_project.features.woekspace.dto.WorkspaceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkspaceMapper {
    // បំប្លែងពី Request (DTO) ទៅជា Entity សម្រាប់ Save
    Workspace fromCreateRequest(WorkspaceCreateRequest request);

    // បំប្លែងពី Entity ទៅជា Response (សម្រាប់បាញ់ចេញទៅវិញ)
    WorkspaceResponse toResponse(Workspace workspace);

    List<WorkspaceResponse> toResponseList(List<Workspace> workspaces);
}
