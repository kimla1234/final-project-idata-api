package com.example.final_project.features.apiScheme;

import com.example.final_project.domain.ApiData;
import com.example.final_project.domain.ApiScheme;
import io.lettuce.core.ScanIterator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiSchemeRepository extends JpaRepository<ApiScheme, Integer> {

    Optional<ApiScheme> findByEndpointUrl(String endpointUrl);
    boolean existsByWorkspaceProjectKeyAndEndpointUrlContaining(String projectKey, String slug);

    boolean existsByEndpointUrl(String s);
    List<ApiScheme> findAllByFolderId(Integer folderId);

    void deleteById(Integer id);
    List<ApiScheme> findByIsPublishedTrueAndNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    Page<ApiScheme> findByIsPublishedTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<ApiScheme> findByIsPublishedTrueOrderByCreatedAtDesc();

    Page<ApiScheme> findByIsPublishedTrue(Pageable pageable);
    Optional<ApiScheme> findByWorkspaceProjectKeyAndApiKey(String projectKey, String apiKey);
    @Query("SELECT s FROM ApiScheme s WHERE s.workspace.projectKey = :projectKey AND s.name = :slug")
    Optional<ApiScheme> findByProjectKeyAndSlug(
            @Param("projectKey") String projectKey,
            @Param("slug") String slug
    );

}

