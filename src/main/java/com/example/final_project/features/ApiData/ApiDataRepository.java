package com.example.final_project.features.ApiData;

import com.example.final_project.domain.ApiData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiDataRepository extends JpaRepository<ApiData, Long> {
    List<ApiData> findAllByApiSchemeId(Integer apiSchemeId);
    @Query("SELECT d FROM ApiData d WHERE d.id = :id AND d.apiScheme.endpointUrl LIKE %:slug%")
    Optional<ApiData> findByIdAndSlug(@Param("id") Long id, @Param("slug") String slug);

    Optional<ApiData> findByIdAndApiSchemeWorkspaceIdAndApiSchemeEndpointUrlContaining(Long id, Integer workspaceId, String slug);
    Optional<ApiData> findByIdAndApiSchemeEndpointUrlContaining(Long id, String slug);

    @Query(value = "SELECT COALESCE(MAX((content->>'id')::long), 0) FROM api_data WHERE api_scheme_id = :schemeId", nativeQuery = true)
    Long findMaxJsonId(@Param("schemeId") Integer schemeId);

    @Query(value = "SELECT * FROM api_data d WHERE d.api_scheme_id = :schemeId AND d.content ->> 'id' = :jsonId", nativeQuery = true)
    Optional<ApiData> findByJsonId(@Param("schemeId") Long schemeId, @Param("jsonId") String jsonId);

    void deleteAllByApiSchemeId(Integer apiSchemeId);

    @Query(value = "SELECT * FROM api_data WHERE api_scheme_id = :schemeId AND json_data->>'email' = :email LIMIT 1", nativeQuery = true)
    Optional<ApiData> findByEmailInJson(@Param("schemeId") Integer schemeId, @Param("email") String email);

    @Query("SELECT d FROM ApiData d WHERE d.apiScheme.id = :schemeId")
    List<ApiData> findBySchemeId(@Param("schemeId") Integer schemeId);
}