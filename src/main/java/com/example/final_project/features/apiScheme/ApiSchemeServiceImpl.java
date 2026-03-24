package com.example.final_project.features.apiScheme;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.domain.Folder;
import com.example.final_project.domain.ProjectAnalytics;
import com.example.final_project.domain.User;
import com.example.final_project.features.ApiData.ApiDataRepository;
import com.example.final_project.features.ProjectAnalytics.ProjectAnalyticsRepository;
import com.example.final_project.features.apiScheme.dto.*;
import com.example.final_project.features.folder.FolderRepository;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.features.woekspace.WorkspaceMemberRepository;
import com.example.final_project.mapper.ApiSchemeMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiSchemeServiceImpl implements ApiSchemeService {

    private final ApiSchemeRepository apiSchemeRepository;
    private final ProjectAnalyticsRepository analyticsRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ApiSchemeMapper apiSchemeMapper;
    private final ApiDataRepository apiDataRepository;



    private static final String API_GATEWAY_PREFIX = "https://api.idata.fit/api/v1/engine-";


    @Override
    @Transactional
    public ApiSchemeResponse createScheme(ApiSchemeCreateRequest request, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        String slug = request.endpointUrl().trim();
        if (slug.startsWith("/")) slug = slug.substring(1);

        String projectKey = folder.getWorkspace().getProjectKey();

        if (apiSchemeRepository.existsByWorkspaceProjectKeyAndEndpointUrlContaining(projectKey, slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug '" + slug + "' មានរួចហើយក្នុង Project របស់អ្នក");
        }

        ApiScheme scheme = apiSchemeMapper.fromCreateRequest(request);
        scheme.setIsPublic(false);

        if (scheme.getApiKey() == null) {
            String newKey = "sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "");
            scheme.setApiKey(newKey);
        }

        String generatedFullUrl = API_GATEWAY_PREFIX + projectKey + "/" + slug;
        scheme.setEndpointUrl(generatedFullUrl);

        User owner = userRepository.findByEmail(email).orElseThrow();
        scheme.setOwner(owner);
        scheme.setFolder(folder);
        scheme.setWorkspace(folder.getWorkspace());

        ApiScheme savedScheme = apiSchemeRepository.save(scheme);

        // Analytics...
        ProjectAnalytics analytics = new ProjectAnalytics();
        analytics.setApiScheme(savedScheme);
        analyticsRepository.save(analytics);

        return apiSchemeMapper.toResponse(savedScheme);
    }

    @Override
    @Transactional
    public ApiSchemeResponse updateScheme(Integer id, ApiSchemeUpdateRequest request, Jwt jwt) {
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema not found"));

        String currentUserEmail = jwt.getClaimAsString("sub");

        System.out.println("DEBUG: Current User Email from Token: " + currentUserEmail);
        System.out.println("DEBUG: Owner Email from DB: " + scheme.getOwner().getEmail());

        if (currentUserEmail == null || !scheme.getOwner().getEmail().equals(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this API");
        }

        apiSchemeMapper.updateFromRequest(request, scheme);
        scheme.setUpdatedAt(LocalDateTime.now());

        if (request.endpointUrl() != null && !request.endpointUrl().isBlank()) {
            String slug = request.endpointUrl().trim();
            if (slug.contains("/")) {
                String[] parts = slug.split("/");
                slug = parts[parts.length - 1];
            }

            String projectKey = scheme.getWorkspace().getProjectKey();
            if (projectKey == null || projectKey.isBlank()) {
                projectKey = "ws-" + scheme.getWorkspace().getId();
            }

            String newFullUrl = API_GATEWAY_PREFIX + projectKey + "/" + slug;

            if (!newFullUrl.equals(scheme.getEndpointUrl())) {
                scheme.setEndpointUrl(newFullUrl);
            }
        }

        ApiScheme saved = apiSchemeRepository.save(scheme);
        return apiSchemeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteScheme(Integer id, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Schema ឡើយ"));

        if (!scheme.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិលុប Schema របស់អ្នកដទៃឡើយ");
        }

        try {
            apiDataRepository.deleteAllByApiSchemeId(id);

            analyticsRepository.deleteByApiSchemeId(id);

            apiSchemeRepository.delete(scheme);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "មិនអាចលុបបានដោយសារបញ្ហា៖ " + e.getMessage());
        }
    }

    @Override
    public List<ApiSchemeResponse> getSchemesByFolder(Integer folderId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Folder ឡើយ"));

        boolean isMember = memberRepository.existsByWorkspaceIdAndUserEmail(folder.getWorkspace().getId(), email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើលទិន្នន័យក្នុង Workspace នេះទេ");
        }

        List<ApiScheme> schemes = apiSchemeRepository.findAllByFolderId(folderId);

        return schemes.stream()
                .map(apiSchemeMapper::toResponse)
                .toList();
    }

    @Override
    public ApiSchemeResponse getSchemeById(Integer id, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Schema ដែលអ្នកចង់បានឡើយ"));

        boolean isMember = memberRepository.existsByWorkspaceIdAndUserEmail(scheme.getWorkspace().getId(), email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើល Schema នេះទេ");
        }

        return apiSchemeMapper.toResponse(scheme);
    }


    @Override
    @Transactional
    public void togglePublicStatus(Integer id, String description, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema not found"));

        if (!scheme.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can publish this API");
        }

        if (description != null && !description.isBlank()) {
            scheme.setDescription(description);
        }


        scheme.setIsPublished(!scheme.getIsPublished());
        scheme.setUpdatedAt(LocalDateTime.now());
        apiSchemeRepository.save(scheme);
    }



    @Override
    public ApiSchemeResponse getPublicDetailBySlug(String endpointUrl) {
        return null;
    }

    @Override
    @Transactional
    public ApiSchemeResponse forkApi(Integer originalApiId, Integer targetFolderId, Jwt jwt) {
        // ១. ទាញយក API ដើម
        ApiScheme original = apiSchemeRepository.findById(originalApiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Original API not found"));

        // ២. ទាញយក Folder គោលដៅ និង Workspace
        Folder targetFolder = folderRepository.findById(targetFolderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target folder not found"));

        String email = jwt.getClaimAsString("sub");
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ៣. បង្កើត Entity ថ្មីដោយចម្លងទិន្នន័យ
        ApiScheme forked = new ApiScheme();
        forked.setName(original.getName() + " (Forked)");

        // រៀបចំ Endpoint URL ថ្មីឱ្យត្រូវតាម Project Key របស់ Workspace ថ្មី
        String projectKey = targetFolder.getWorkspace().getProjectKey();
        // កាត់យក Slug ចុងក្រោយពី Original URL
        String originalUrl = original.getEndpointUrl();
        String slug = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
        String newFullUrl = API_GATEWAY_PREFIX + projectKey + "/" + slug + "-fork-" + System.currentTimeMillis();

        forked.setEndpointUrl(newFullUrl);
        forked.setProperties(original.getProperties());
        forked.setKeys(original.getKeys());
        forked.setDescription(original.getDescription());

        // 🎯 កំណត់ម្ចាស់ និងទីតាំងថ្មី (ចំណុចដែលបងខ្វះ)
        forked.setOwner(currentUser);
        forked.setFolder(targetFolder);
        forked.setWorkspace(targetFolder.getWorkspace());
        forked.setParentApi(original); // ចងភ្ជាប់ទៅកាន់ original
        forked.setIsPublished(false);  // កុំឱ្យវា Publish អូតូ
        forked.setIsPublic(false);
        forked.setCreatedAt(LocalDateTime.now());

        // ៤. បង្កើត API Key ថ្មីសម្រាប់អ្នក Fork
        String newKey = "sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "");
        forked.setApiKey(newKey);

        // ៥. បង្កើន forkCount ឱ្យ original API
        original.setForkCount((original.getForkCount() == null ? 0 : original.getForkCount()) + 1);
        apiSchemeRepository.save(original);

        // ៦. បង្កើត Analytics សម្រាប់ API ថ្មី
        ApiScheme savedForked = apiSchemeRepository.save(forked);
        ProjectAnalytics analytics = new ProjectAnalytics();
        analytics.setApiScheme(savedForked);
        analyticsRepository.save(analytics);

        return apiSchemeMapper.toResponse(savedForked);
    }


    @Override
    public ApiSchemeStatsResponse getSchemeStats(Integer id, Jwt jwt) {
        return null;
    }

    @Override
    @Transactional
    public ApiSchemeResponse create(ApiSchemeRequest request, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Folder ឡើយ"));

        String slug = request.endpointUrl().trim()
                .replace("/", "")
                .toLowerCase();

        String projectKey = folder.getWorkspace().getProjectKey();
        String generatedFullUrl = API_GATEWAY_PREFIX + projectKey + "/" + slug;

        ApiScheme scheme = new ApiScheme();
        scheme.setName(request.name());
        scheme.setEndpointUrl(generatedFullUrl);
        scheme.setDescription(request.description());
        scheme.setProperties(request.properties());
        scheme.setKeys(request.keys());
        scheme.setIsPublic(request.isPublic());
        scheme.setEndpointUrl(slug);

        User owner = userRepository.findByEmail(email).orElseThrow();
        scheme.setOwner(owner);
        scheme.setFolder(folder);
        scheme.setWorkspace(folder.getWorkspace());
        scheme.setCreatedAt(LocalDateTime.now());

        ApiScheme savedScheme = apiSchemeRepository.save(scheme);

        ProjectAnalytics analytics = new ProjectAnalytics();
        analytics.setApiScheme(savedScheme);
        analyticsRepository.save(analytics);

        return apiSchemeMapper.toResponse(savedScheme);
    }

    @Override
    public ApiSchemeResponse getPublicDetailById(Integer id) {
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "រកមិនឃើញ API Schema ដែលអ្នកចង់បានឡើយ"
                ));

        if (!scheme.getIsPublic()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "API Schema នេះមិនទាន់ត្រូវបានដាក់ជាសាធារណៈ (Public) នៅឡើយទេ"
            );
        }

        return apiSchemeMapper.toResponse(scheme);
    }


    @Override
    public List<ApiSchemeResponse> getCommunityFeed(String search, int page, int size, Jwt jwt) {
        User currentUser = null;
        if (jwt != null) {
            String email = jwt.getClaimAsString("sub");
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("forkCount").descending().and(Sort.by("createdAt").descending()));
        Page<ApiScheme> schemesPage;

        if (search != null && !search.trim().isEmpty()) {
            schemesPage = apiSchemeRepository.findByIsPublishedTrueAndNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            schemesPage = apiSchemeRepository.findByIsPublishedTrue(pageable);
        }

        User finalCurrentUser = currentUser;
        return schemesPage.getContent().stream()
                .map(scheme -> {
                    ApiSchemeResponse res = apiSchemeMapper.toResponse(scheme);

                    return res.withIsFollowed(true);
                })
                .toList();
    }

    public ApiSchemeResponse toResponseWithStatus(ApiScheme scheme, User currentUser) {
        ApiSchemeResponse response = apiSchemeMapper.toResponse(scheme);

        boolean followed = scheme.getOwner().getFollowers().stream()
                .anyMatch(f -> f.getId().equals(currentUser.getId()));

        response.withIsFollowed(followed);

        return response;
    }
}