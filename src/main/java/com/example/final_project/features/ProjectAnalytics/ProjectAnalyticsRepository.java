package com.example.final_project.features.ProjectAnalytics;

import com.example.final_project.domain.ProjectAnalytics;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface ProjectAnalyticsRepository extends JpaRepository<ProjectAnalytics, Integer> {
    ProjectAnalytics findByApiSchemeId(Integer id);

    @Modifying
    @Transactional
    @Query("DELETE FROM ApiScheme a WHERE a.id = :id")
    void deleteByApiSchemeId(@Param("id") Integer id);
}
