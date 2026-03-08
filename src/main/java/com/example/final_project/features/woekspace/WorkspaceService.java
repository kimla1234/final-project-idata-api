package com.example.final_project.features.woekspace;

import com.example.final_project.domain.User;
import com.example.final_project.features.woekspace.dto.*;
import com.nimbusds.jwt.JWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WorkspaceService {

    WorkspaceResponse createWorkspace (WorkspaceCreateRequest workspaceCreateRequest , Jwt jwt);

    List<WorkspaceResponse> getMyWorkspaces(Jwt jwt);
    WorkspaceResponse getWorkspaceById(Integer id, Jwt jwt);
    void inviteMember(Integer workspaceId, WorkspaceInviteRequest request, Jwt jwt);
    // WorkspaceService.java
    void updateMemberRole(Integer workspaceId, Integer memberId, WorkspaceRoleUpdateRequest request, Jwt jwt);
    // WorkspaceService.java
    List<MemberResponse> getWorkspaceMembers(Integer workspaceId, Jwt jwt);

    void deleteWorkspace(Integer id, DeleteWorkspaceRequest request, Jwt jwt);
    WorkspaceResponse updateWorkspace(Integer id, WorkspaceUpdateRequest request, Jwt jwt);
    WorkspaceStatsResponse getAllCampaigns(Integer workspaceId, Jwt jwt);
}
