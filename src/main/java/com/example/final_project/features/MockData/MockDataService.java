package com.example.final_project.features.MockData;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public interface MockDataService {
    // បញ្ចូលទិន្នន័យគំរូ (Validate ជាមួយ Schema ជាមុន)
    Map<String, Object> addMockData(String endpointUrl, Map<String, Object> data, Jwt jwt);

    // ទាញយកទិន្នន័យទាំងអស់ដែលស្ថិតក្រោម API Scheme មួយ
    List<Map<String, Object>> getMockDataByEndpoint(String endpointUrl);

    // លុបទិន្នន័យចោលទាំងអស់ក្នុង API នោះ (Clear Data)
    void clearMockData(String endpointUrl, Jwt jwt);
}
