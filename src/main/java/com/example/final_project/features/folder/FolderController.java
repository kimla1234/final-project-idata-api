package com.example.final_project.features.folder;

import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/folders")
@RequiredArgsConstructor
@Hidden
public class FolderController {
    private final FolderService folderService;
    // ១. បង្កើត Folder ថ្មីនៅក្នុង Workspace
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(
            @PathVariable Integer workspaceId,
            @Valid @RequestBody FolderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return folderService.createFolder(workspaceId, request, jwt);
    }

    // ២. ទាញយកបញ្ជី Folder ទាំងអស់នៃ Workspace ជាក់លាក់មួយ
    @GetMapping
    public List<FolderResponse> getFoldersByWorkspace(
            @PathVariable Integer workspaceId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return folderService.getFoldersByWorkspace(workspaceId, jwt);
    }

    @PutMapping("/{folderId}")
    public FolderResponse updateFolder(
            @PathVariable Integer folderId,
            @Valid @RequestBody FolderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return folderService.updateFolder(folderId, request, jwt);
    }

    // បន្ថែម Delete Folder
    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(
            @PathVariable Integer workspaceId,
            @PathVariable Integer folderId,
            @AuthenticationPrincipal Jwt jwt) {
        folderService.deleteFolder(folderId, jwt);
    }
}
