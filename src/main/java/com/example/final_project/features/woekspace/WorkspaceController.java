package com.example.final_project.features.woekspace;

import com.example.final_project.domain.Workspace;
import com.example.final_project.features.campaign.MailingService;
import com.example.final_project.features.user.UserService;
import com.example.final_project.features.woekspace.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;
    private final MailingService mailingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return workspaceService.createWorkspace(request, jwt);
    }

    @GetMapping("/my")
    public List<WorkspaceResponse> getMyWorkspaces(@AuthenticationPrincipal Jwt jwt) {
        return workspaceService.getMyWorkspaces(jwt);
    }

    @GetMapping("/{id}")
    public WorkspaceResponse getWorkspaceById(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return workspaceService.getWorkspaceById(id, jwt);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkspace(
            @PathVariable Integer id,
            @Valid @RequestBody DeleteWorkspaceRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        workspaceService.deleteWorkspace(id, request, jwt);
    }

    @PostMapping("/{id}/invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inviteMember(
            @PathVariable Integer id,
            @Valid @RequestBody WorkspaceInviteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        workspaceService.inviteMember(id, request, jwt);
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> getWorkspaceMembers(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return workspaceService.getWorkspaceMembers(id, jwt);
    }


    @GetMapping("/{workspaceId}/summary")
    public WorkspaceStatsResponse getSummary(
            @PathVariable Integer workspaceId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return workspaceService.getAllCampaigns(workspaceId, jwt);
    }

    @PutMapping("/{id}")
    public WorkspaceResponse updateWorkspace(
            @PathVariable Integer id,
            @Valid @RequestBody WorkspaceUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return workspaceService.updateWorkspace(id, request, jwt);
    }

    // បន្ថែមក្នុង WorkspaceController.java

    @PatchMapping("/{id}/members/{memberId}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemberRole(
            @PathVariable Integer id,        // workspaceId
            @PathVariable Integer memberId,  // memberId
            @Valid @RequestBody WorkspaceRoleUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        workspaceService.updateMemberRole(id, memberId, request, jwt);
    }

}
