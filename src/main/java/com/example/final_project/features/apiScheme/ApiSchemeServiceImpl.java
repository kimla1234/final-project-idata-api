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


    // កំណត់ Domain របស់ API Engine (ឧទាហរណ៍)

    // នៅក្នុង ApiSchemeServiceImpl.java
// កំណត់ Base URL ដែល User នឹងយកទៅប្រើ (ឧទាហរណ៍៖ https://api.idata.com/v1/...)
    private static final String API_GATEWAY_PREFIX = "http://localhost:8081/api/v1/engine-";


    @Override
    @Transactional
    public ApiSchemeResponse createScheme(ApiSchemeCreateRequest request, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // ១. សម្អាត Slug
        String slug = request.endpointUrl().trim();
        if (slug.startsWith("/")) slug = slug.substring(1);

        // ២. ទាញយក projectKey ពី Workspace
        String projectKey = folder.getWorkspace().getProjectKey();

        // ៣. ឆែក Unique Slug ក្នុង Project តែមួយ (User ផ្សេងមាន Slug ដូចគ្នាក៏មិនអីដែរ)
        if (apiSchemeRepository.existsByWorkspaceProjectKeyAndEndpointUrlContaining(projectKey, slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug '" + slug + "' មានរួចហើយក្នុង Project របស់អ្នក");
        }

        ApiScheme scheme = apiSchemeMapper.fromCreateRequest(request);
        scheme.setIsPublic(false);

        if (scheme.getApiKey() == null) {
            String newKey = "sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "");
            scheme.setApiKey(newKey);
        }

        // ៤. បង្កើត Full URL រាង៖ http://localhost:8080/api/v1/engine-abc123/users
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
        // ១. ទាញយក Entity
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema not found"));

        // 🎯 ២. កែសម្រួល Key ឱ្យទៅជា "sub" ដូចក្នុង createScheme វិញ
        String currentUserEmail = jwt.getClaimAsString("sub");

        // បន្ថែម Log ដើម្បី Check មើលថាវាទាញបាន Email ពិតមែនអត់ (មើលក្នុង Console)
        System.out.println("DEBUG: Current User Email from Token: " + currentUserEmail);
        System.out.println("DEBUG: Owner Email from DB: " + scheme.getOwner().getEmail());

        if (currentUserEmail == null || !scheme.getOwner().getEmail().equals(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this API");
        }

        // ៣. Update ទិន្នន័យ (isPublic នឹងត្រូវបាន Update តាមរយៈ mapper)
        apiSchemeMapper.updateFromRequest(request, scheme);
        scheme.setUpdatedAt(LocalDateTime.now());

        // ៤. Logic សម្រាប់ Endpoint URL
        if (request.endpointUrl() != null && !request.endpointUrl().isBlank()) {
            // កាត់យកតែ Slug ចុងក្រោយ ដើម្បីកុំឱ្យជាប់ Validation Regex ^[a-z0-9-]+$
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
    @Transactional // 🎯 ត្រូវតែមាន Transactional ដើម្បីឱ្យវាលុបទាំងមេទាំងកូនក្នុងពេលតែមួយ
    public void deleteScheme(Integer id, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Schema ឡើយ"));

        if (!scheme.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិលុប Schema របស់អ្នកដទៃឡើយ");
        }

        // 🎯 ជំហានសំខាន់៖ លុបទិន្នន័យពាក់ព័ន្ធទាំងអស់មុននឹងលុប Schema
        try {
            // ១. លុបទិន្នន័យដែលបាន Generate (ApiData)
            apiDataRepository.deleteAllByApiSchemeId(id);

            // ២. លុប Analytics (បើមាន)
            analyticsRepository.deleteByApiSchemeId(id);

            // ៣. លុប Schema ផ្ទាល់
            apiSchemeRepository.delete(scheme);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "មិនអាចលុបបានដោយសារបញ្ហា៖ " + e.getMessage());
        }
    }

    @Override
    public List<ApiSchemeResponse> getSchemesByFolder(Integer folderId, Jwt jwt) {
        // ១. ទាញយក Email ពី Token
        String email = jwt.getClaimAsString("sub");

        // ២. ស្វែងរក Folder ដើម្បីដឹងថាវាស្ថិតក្នុង Workspace ណា
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Folder ឡើយ"));

        // ៣. ឆែកមើលសិទ្ធិ (Access Control)
        // អ្នកប្រើប្រាស់ត្រូវតែជាសមាជិកនៃ Workspace នោះ ទើបអាចមើល API ក្នុង Folder នោះបាន
        boolean isMember = memberRepository.existsByWorkspaceIdAndUserEmail(folder.getWorkspace().getId(), email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើលទិន្នន័យក្នុង Workspace នេះទេ");
        }

        // ៤. ទាញយកបញ្ជី API Schemes ទាំងអស់ក្នុង Folder នោះ
        List<ApiScheme> schemes = apiSchemeRepository.findAllByFolderId(folderId);

        // ៥. បំប្លែងពី Entity ទៅជា Response DTO ដោយប្រើ Mapper
        return schemes.stream()
                .map(apiSchemeMapper::toResponse)
                .toList();
    }

    @Override
    public ApiSchemeResponse getSchemeById(Integer id, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        // ១. ស្វែងរក ApiScheme តាម ID
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Schema ដែលអ្នកចង់បានឡើយ"));

        // ២. ឆែកមើលសិទ្ធិ៖ អ្នកប្រើប្រាស់ត្រូវតែជាសមាជិកនៃ Workspace ដែល Schema នោះស្ថិតនៅ
        boolean isMember = memberRepository.existsByWorkspaceIdAndUserEmail(scheme.getWorkspace().getId(), email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើល Schema នេះទេ");
        }

        // ៣. បំប្លែងទៅជា Response DTO
        return apiSchemeMapper.toResponse(scheme);
    }

    // ApiSchemeServiceImpl.java

    @Override
    @Transactional
    public void togglePublicStatus(Integer id, String description, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema not found"));

        if (!scheme.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can publish this API");
        }

        // បើគាត់កំពុង Publish (isPublished false -> true) ឱ្យ Update Description ថ្មី
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

        // ១. ប្រើ request.folderId() (គ្មានពាក្យ get)
        Folder folder = folderRepository.findById(request.folderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Folder ឡើយ"));

        // ២. ប្រើ request.endpointUrl()
        String slug = request.endpointUrl().trim()
                .replace("/", "")  // លុប / ចេញទាំងអស់
                .toLowerCase();    // ប្តូរជាអក្សរតូចដើម្បីសុវត្ថិភាព

        String projectKey = folder.getWorkspace().getProjectKey();
        String generatedFullUrl = API_GATEWAY_PREFIX + projectKey + "/" + slug;

        // ៣. បង្កើត Entity និង Map ទិន្នន័យ
        ApiScheme scheme = new ApiScheme();
        scheme.setName(request.name()); // ✅ ត្រូវ
        scheme.setEndpointUrl(generatedFullUrl);
        scheme.setDescription(request.description()); // ✅ ត្រូវ
        scheme.setProperties(request.properties());   // ✅ ត្រូវ
        scheme.setKeys(request.keys());               // ✅ ត្រូវ
        scheme.setIsPublic(request.isPublic());       // ✅ ត្រូវ
        scheme.setEndpointUrl(slug);

        // ៤. កំណត់សមាជិកពាក់ព័ន្ធ
        User owner = userRepository.findByEmail(email).orElseThrow();
        scheme.setOwner(owner);
        scheme.setFolder(folder);
        scheme.setWorkspace(folder.getWorkspace());
        scheme.setCreatedAt(LocalDateTime.now());

        ApiScheme savedScheme = apiSchemeRepository.save(scheme);

        // បង្កើត Analytics
        ProjectAnalytics analytics = new ProjectAnalytics();
        analytics.setApiScheme(savedScheme);
        analyticsRepository.save(analytics);

        return apiSchemeMapper.toResponse(savedScheme);
    }

    @Override
    public ApiSchemeResponse getPublicDetailById(Integer id) {
        // ១. ស្វែងរក ApiScheme តាម ID
        ApiScheme scheme = apiSchemeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "រកមិនឃើញ API Schema ដែលអ្នកចង់បានឡើយ"
                ));

        // ២. 🎯 លក្ខខណ្ឌការពារ៖ បើ API មិនទាន់បាន Publish ទេ មិនឱ្យអ្នកដទៃមើលឡើយ
        if (!scheme.getIsPublic()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "API Schema នេះមិនទាន់ត្រូវបានដាក់ជាសាធារណៈ (Public) នៅឡើយទេ"
            );
        }

        // ៣. បំប្លែងទៅជា Response DTO រួចបោះទៅឱ្យ Controller
        return apiSchemeMapper.toResponse(scheme);
    }

    // ApiSchemeServiceImpl.java

    @Override
    public List<ApiSchemeResponse> getCommunityFeed(String search, int page, int size, Jwt jwt) {
        // ១. ទាញយក Current User ពី Token (បើអត់មាន Token គឺ null)
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
        // ក្នុង ApiSchemeServiceImpl.java ត្រង់ getCommunityFeed
        return schemesPage.getContent().stream()
                .map(scheme -> {
                    // ១. Map ជាមុន
                    ApiSchemeResponse res = apiSchemeMapper.toResponse(scheme);

                    // ២. តេស្ត Hardcode សិន (កុំទាន់ប្រើ Logic anyMatch)
                    // បើ JSON ចេញ true មានន័យថា Record ដើរហើយ
                    return res.withIsFollowed(true);
                })
                .toList();
    }

    // ឧទាហរណ៍ក្នុង Service ពេលទាញយក Community Feed
    public ApiSchemeResponse toResponseWithStatus(ApiScheme scheme, User currentUser) {
        ApiSchemeResponse response = apiSchemeMapper.toResponse(scheme);

        // ឆែកមើលថា តើ currentUser មាននៅក្នុងបញ្ជី Followers របស់ Owner ដែរឬទេ
        boolean followed = scheme.getOwner().getFollowers().stream()
                .anyMatch(f -> f.getId().equals(currentUser.getId()));

        // បងត្រូវថែម field "Boolean isFollowed" ក្នុង ApiSchemeResponse DTO ផង
        response.withIsFollowed(followed);

        return response;
    }
}