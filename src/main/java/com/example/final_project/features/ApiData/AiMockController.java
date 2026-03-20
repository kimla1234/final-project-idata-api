package com.example.final_project.features.ApiData;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Hidden
public class AiMockController {
    private final MockDataService mockDataService;
    private final ApiSchemeRepository apiSchemeRepository;

    @PostMapping("/mock/{id}")
    public ResponseEntity<?> handleAiMock(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            ApiScheme schema = apiSchemeRepository.findById(Math.toIntExact(id))
                    .orElseThrow(() -> new RuntimeException("Schema not found with id: " + id));

            mockDataService.generateAndSave(schema, body.get("instruction"));

            return ResponseEntity.ok(Map.of("status", "success", "message", "Mock data generated successfully"));
        } catch (Exception e) {
            // 🎯 ត្រូវបោះត្រឡប់ទៅវិញជា Map ដើម្បីឱ្យ Jackson បំប្លែងទៅជា JSON {"error": "..."}
            // ធ្វើបែបនេះ Frontend នឹងលែងជួប PARSING_ERROR ទៀតហើយ
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}