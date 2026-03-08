package com.example.final_project.features.folder;

import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FolderService {
    FolderResponse createFolder(Integer workspaceId, FolderRequest request, Jwt jwt);

    // មើល Folder ទាំងអស់ក្នុង Workspace មួយ
    List<FolderResponse> getFoldersByWorkspace(Integer workspaceId, Jwt jwt);
    // បន្ថែម Method នេះសម្រាប់កែប្រែ Folder
    FolderResponse updateFolder(Integer folderId, FolderRequest request, Jwt jwt);
    void deleteFolder(Integer folderId, Jwt jwt);
}
