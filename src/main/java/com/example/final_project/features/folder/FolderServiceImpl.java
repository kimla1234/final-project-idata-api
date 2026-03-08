package com.example.final_project.features.folder;

import com.example.final_project.domain.Folder;
import com.example.final_project.domain.Workspace;
import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import com.example.final_project.features.woekspace.WorkspaceMemberRepository;
import com.example.final_project.features.woekspace.WorkspaceRepository;
import com.example.final_project.mapper.FolderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final FolderMapper folderMapper;

    @Override
    @Transactional
    public FolderResponse createFolder(Integer workspaceId, FolderRequest request, Jwt jwt) {
        // ១. ឆែកមើលថា User ជាសមាជិក Workspace ហ្នឹងមែនអត់
        String email = jwt.getClaimAsString("sub");
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email);

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិបង្កើត Folder ក្នុង Workspace នេះទេ");
        }

        // ២. ទាញយក Workspace Entity
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Workspace"));

        // ៣. រក្សាទុក Folder
        Folder folder = folderMapper.fromRequest(request);
        folder.setWorkspace(workspace);

        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Override
    public List<FolderResponse> getFoldersByWorkspace(Integer workspaceId, Jwt jwt) {
        // ឆែកសិទ្ធិមុននឹងឱ្យ List ទៅកាន់ User
        String email = jwt.getClaimAsString("sub");
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        return folderMapper.toResponseList(folderRepository.findAllByWorkspaceId(workspaceId));
    }

    @Override
    public FolderResponse updateFolder(Integer folderId, FolderRequest request, Jwt jwt) {
        // ១. ស្វែងរក Folder ក្នុង Database
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // ២. (Optional) ឆែក Security ថា User ហ្នឹងមានសិទ្ធិកែក្នុង Workspace ហ្នឹងអត់
        // String email = jwt.getClaimAsString("email");
        // ... logic ឆែក ownership ...

        // ៣. កែឈ្មោះថ្មី
        folder.setName(request.name());

        // ៤. រក្សាទុកចូល Database វិញ
        folder = folderRepository.save(folder);

        return folderMapper.toResponse(folder);
    }

    @Override
    public void deleteFolder(Integer folderId, Jwt jwt) {
        if (!folderRepository.existsById(folderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found");
        }
        folderRepository.deleteById(folderId);
    }
}