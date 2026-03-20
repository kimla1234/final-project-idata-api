package com.example.final_project.features.MockData;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mock") // 'm' តំណាងឱ្យ Mock Endpoint
@RequiredArgsConstructor
@Hidden
public class MockDataController {
    private final MockDataService mockDataService;

    @PostMapping("/{endpointUrl}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addMockData(
            @PathVariable String endpointUrl,
            @Valid @RequestBody Map<String, Object> data, // ទទួល Dynamic JSON
            @AuthenticationPrincipal Jwt jwt) {
        return mockDataService.addMockData(endpointUrl, data, jwt);
    }

    @GetMapping("/{endpointUrl}")
    public List<Map<String, Object>> getMockData(@PathVariable String endpointUrl) {
        return mockDataService.getMockDataByEndpoint(endpointUrl);
    }
}
