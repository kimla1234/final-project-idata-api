package com.example.final_project.features.MockData;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.domain.MockData;
import com.example.final_project.domain.ProjectAnalytics;
import com.example.final_project.features.ProjectAnalytics.ProjectAnalyticsRepository;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MockDataServiceImpl implements MockDataService  {
    private final MockDataRepository mockDataRepository;
    private final ApiSchemeRepository apiSchemeRepository;
    private final ProjectAnalyticsRepository analyticsRepository;


    @Override
    @Transactional
    public Map<String, Object> addMockData(String endpointUrl, Map<String, Object> requestData, Jwt jwt) {
        ApiScheme apiScheme = apiSchemeRepository.findByEndpointUrl(endpointUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ API នេះឡើយ"));

        List<Map<String, Object>> properties = apiScheme.getProperties();
        if (properties == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "API មិនទាន់មានការកំណត់ Properties");
        }

        for (Map<String, Object> prop : properties) {
            String fieldName = (String) prop.get("fieldName");
            Boolean isRequired = (Boolean) prop.get("required");
            if (Boolean.TRUE.equals(isRequired) && !requestData.containsKey(fieldName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ខ្វះ Field ចាំបាច់៖ " + fieldName);
            }
        }

        MockData mockData = new MockData();
        mockData.setApiScheme(apiScheme);
        mockData.setDataJson(requestData);

        mockDataRepository.save(mockData);

        updateAnalytics(apiScheme);

        return requestData;
    }


    @Override
    public List<Map<String, Object>> getMockDataByEndpoint(String endpointUrl) {
        ApiScheme scheme = apiSchemeRepository.findByEndpointUrl(endpointUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API not found"));

        updateAnalytics(scheme);

        return mockDataRepository.findAllByApiSchemeId(scheme.getId())
                .stream()
                .map(MockData::getDataJson)
                .toList();
    }

    @Override
    public void clearMockData(String endpointUrl, Jwt jwt) {

    }


    // --- Helper Methods ---

    private void validateIncomingData(Map<String, Object> incoming, Map<String, Object> schema) {
        for (String key : schema.keySet()) {
            if (!incoming.containsKey(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ខ្វះ Field: " + key);
            }
        }
    }

    private void updateAnalytics(ApiScheme scheme) {
        ProjectAnalytics analytics = analyticsRepository.findByApiSchemeId(scheme.getId());
        if (analytics != null) {
            analytics.setTotalRequests(analytics.getTotalRequests() + 1);
            analytics.setLastAccessedAt(LocalDateTime.now());
            analyticsRepository.save(analytics);
        }
    }
}
