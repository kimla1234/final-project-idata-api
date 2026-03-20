package com.example.final_project.features.apiScheme;

import com.example.final_project.features.apiScheme.dto.ApiSchemeCreateRequest;
import com.example.final_project.features.apiScheme.dto.ApiSchemeResponse;
import com.example.final_project.features.apiScheme.dto.ApiSchemeUpdateRequest;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/api-schemes")
@RequiredArgsConstructor
@Hidden
public class ApiSchemeController {

    private final ApiSchemeService apiSchemeService;

    // --- ផ្នែកគ្រប់គ្រងក្នុង Workspace (Private) ---

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSchemeResponse createScheme(@Valid @RequestBody ApiSchemeCreateRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return apiSchemeService.createScheme(request, jwt);
    }

    @GetMapping("/folder/{folderId}")
    public List<ApiSchemeResponse> getSchemesByFolder(@PathVariable Integer folderId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return apiSchemeService.getSchemesByFolder(folderId, jwt);
    }

    @GetMapping("/{id}")
    public ApiSchemeResponse getSchemeById(@PathVariable Integer id,
                                           @AuthenticationPrincipal Jwt jwt) {
        return apiSchemeService.getSchemeById(id, jwt);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // បន្ថែម Status 204 បើលុបជោគជ័យ
    void deleteScheme(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) { // ថែម @AuthenticationPrincipal
        apiSchemeService.deleteScheme(id, jwt);
    }

    @PutMapping("/{id}")
    public ApiSchemeResponse updateScheme(@PathVariable Integer id,
                                          @Valid @RequestBody ApiSchemeUpdateRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return apiSchemeService.updateScheme(id, request, jwt);
    }

    @PatchMapping("/{id}/publish")
    public void togglePublicStatus(@PathVariable Integer id,
                                   @RequestParam(required = false) String description,
                                   @AuthenticationPrincipal Jwt jwt) {
        apiSchemeService.togglePublicStatus(id, description, jwt);
    }

    // --- ផ្នែកសហគមន៍ (Public - អ្នកណាក៏មើលបាន) ---


    @GetMapping("/public/feed")
    public List<ApiSchemeResponse> getCommunityFeed(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt // 🎯 ថែម Jwt កន្លែងនេះ
    ) {
        // បោះ Search, Page, Size និង Jwt ទៅឱ្យ Service
        return apiSchemeService.getCommunityFeed(search, page, size, jwt);
    }

    @GetMapping("/public/{id}")
    public ApiSchemeResponse getPublicDetail(@PathVariable Integer id) {
        // បង្កើត Method ថ្មីក្នុង Service ដែលមិនត្រូវការ Jwt (Security PermitAll)
        return apiSchemeService.getPublicDetailById(id);
    }

    @PostMapping("/{id}/fork") // 🎯 ត្រូវតែជា PostMapping
    public ResponseEntity<ApiSchemeResponse> forkApi(
            @PathVariable Integer id,
            @RequestParam Integer targetFolderId, // 🎯 ទទួលតាម Query Param (?targetFolderId=...)
            @AuthenticationPrincipal Jwt jwt) {

        ApiSchemeResponse response = apiSchemeService.forkApi(id, targetFolderId, jwt);
        return ResponseEntity.ok(response);
    }


}
