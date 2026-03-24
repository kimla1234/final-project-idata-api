package com.example.final_project.features.apiScheme;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.apiScheme.dto.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface ApiSchemeService  {
    ApiSchemeResponse createScheme(ApiSchemeCreateRequest request, Jwt jwt);

    ApiSchemeResponse updateScheme(Integer id, ApiSchemeUpdateRequest request, Jwt jwt);

    void deleteScheme(Integer id, Jwt jwt);

    List<ApiSchemeResponse> getSchemesByFolder(Integer folderId, Jwt jwt);

    ApiSchemeResponse getSchemeById(Integer id, Jwt jwt);

    void togglePublicStatus(Integer id, String description, Jwt jwt); // បើក/បិទ ការផ្សព្វផ្សាយជាសាធារណៈ


    ApiSchemeResponse getPublicDetailBySlug(String endpointUrl); // មើល Detail របស់ Public API

    ApiSchemeResponse forkApi(Integer originalApiId, Integer targetFolderId, Jwt jwt);

    ApiSchemeStatsResponse getSchemeStats(Integer id, Jwt jwt);

    ApiSchemeResponse create(ApiSchemeRequest request, Jwt jwt);

    ApiSchemeResponse getPublicDetailById(Integer id);




    List<ApiSchemeResponse> getCommunityFeed(String search, int page, int size ,Jwt jwt);
}
