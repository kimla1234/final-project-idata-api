package com.example.final_project.features.MockData;

import com.example.final_project.domain.MockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockDataRepository extends JpaRepository<MockData, Integer> {
    // ១. សម្រាប់ទាញយកទិន្នន័យ JSON ទាំងអស់ដែលពាក់ព័ន្ធនឹង API Scheme មួយ
    List<MockData> findAllByApiSchemeId(Integer apiSchemeId);

    // ២. សម្រាប់លុបទិន្នន័យចោលទាំងអស់ក្នុង API នោះ (Clear Data)
    @Modifying
    @Query("DELETE FROM MockData m WHERE m.apiScheme.id = :apiSchemeId")
    void deleteAllByApiSchemeId(Integer apiSchemeId);
}
