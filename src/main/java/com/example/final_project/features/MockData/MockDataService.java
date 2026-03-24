package com.example.final_project.features.MockData;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public interface MockDataService {
    Map<String, Object> addMockData(String endpointUrl, Map<String, Object> data, Jwt jwt);

    List<Map<String, Object>> getMockDataByEndpoint(String endpointUrl);

    void clearMockData(String endpointUrl, Jwt jwt);
}
