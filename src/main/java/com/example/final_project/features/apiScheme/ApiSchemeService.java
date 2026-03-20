package com.example.final_project.features.apiScheme;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.apiScheme.dto.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface ApiSchemeService  {
    // ១. ការគ្រប់គ្រងក្នុង Workspace (Private)
    ApiSchemeResponse createScheme(ApiSchemeCreateRequest request, Jwt jwt);

    ApiSchemeResponse updateScheme(Integer id, ApiSchemeUpdateRequest request, Jwt jwt);

    void deleteScheme(Integer id, Jwt jwt);

    List<ApiSchemeResponse> getSchemesByFolder(Integer folderId, Jwt jwt);

    ApiSchemeResponse getSchemeById(Integer id, Jwt jwt);

    // ២. មុខងារ Public & Community (Behance Style)
    void togglePublicStatus(Integer id, String description, Jwt jwt); // បើក/បិទ ការផ្សព្វផ្សាយជាសាធារណៈ


    ApiSchemeResponse getPublicDetailBySlug(String endpointUrl); // មើល Detail របស់ Public API

    // ៣. មុខងារ Forking (បេះដូងនៃ iDATA)
    ApiSchemeResponse forkApi(Integer originalApiId, Integer targetFolderId, Jwt jwt);

    // ៤. មុខងារ Analytics (តាមដានការប្រើប្រាស់)
    ApiSchemeStatsResponse getSchemeStats(Integer id, Jwt jwt);

    // ក្នុង ApiSchemeService.java
    ApiSchemeResponse create(ApiSchemeRequest request, Jwt jwt);

    ApiSchemeResponse getPublicDetailById(Integer id);




    // បន្ថែម Parameter search និង category (អាចដាក់ null បានបើមិនប្រើ)
    //List<ApiSchemeResponse> getCommunityFeed(String search, String category);
    List<ApiSchemeResponse> getCommunityFeed(String search, int page, int size ,Jwt jwt);
}
