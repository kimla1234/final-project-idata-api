package com.example.final_project.features.MockData;

import com.example.final_project.domain.MockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockDataRepository extends JpaRepository<MockData, Integer> {
    List<MockData> findAllByApiSchemeId(Integer apiSchemeId);

    @Modifying
    @Query("DELETE FROM MockData m WHERE m.apiScheme.id = :apiSchemeId")
    void deleteAllByApiSchemeId(Integer apiSchemeId);
}
