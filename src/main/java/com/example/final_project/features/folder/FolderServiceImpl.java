package com.example.final_project.features.folder;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.domain.Folder;
import com.example.final_project.domain.Workspace;
import com.example.final_project.features.ApiData.ApiDataRepository;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import com.example.final_project.features.folder.dto.FolderRequest;
import com.example.final_project.features.folder.dto.FolderResponse;
import com.example.final_project.features.woekspace.WorkspaceMemberRepository;
import com.example.final_project.features.woekspace.WorkspaceRepository;
import com.example.final_project.mapper.FolderMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
    private  final ApiDataRepository apiDataRepository;
    private final ApiSchemeRepository apiSchemeRepository;
    private final ApiSchemeRepository   analyticsRepository;

    @Override
    @Transactional
    public FolderResponse createFolder(Integer workspaceId, FolderRequest request, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email);

        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិបង្កើត Folder ក្នុង Workspace នេះទេ");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Workspace"));

        Folder folder = folderMapper.fromRequest(request);
        folder.setWorkspace(workspace);

        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Override
    public List<FolderResponse> getFoldersByWorkspace(Integer workspaceId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        return folderMapper.toResponseList(folderRepository.findAllByWorkspaceId(workspaceId));
    }

    @Override
    public FolderResponse updateFolder(Integer folderId, FolderRequest request, Jwt jwt) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));


        folder.setName(request.name());

        folder = folderRepository.save(folder);

        return folderMapper.toResponse(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Integer folderId, Jwt jwt) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Folder ឡើយ"));


        List<ApiScheme> schemes = apiSchemeRepository.findAllByFolderId(folderId);

        try {
            for (ApiScheme scheme : schemes) {

                apiDataRepository.deleteAllByApiSchemeId(scheme.getId());

                analyticsRepository.deleteById(scheme.getId());

                apiSchemeRepository.delete(scheme);
            }

            folderRepository.delete(folder);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "មិនអាចលុប Folder បានទេ ដោយសារបញ្ហាបច្ចេកទេស៖ " + e.getMessage());
        }
    }
}