package com.example.final_project.features.FileSchemaUpload;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.apiScheme.ApiSchemeService;
import com.example.final_project.features.apiScheme.dto.ApiSchemeRequest;
import com.example.final_project.features.apiScheme.dto.ApiSchemeResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/generate-file")
@RequiredArgsConstructor
@Hidden
public class FileUploadController {

    private final FileSchemaService fileSchemaService;
    private final ApiSchemeService apiSchemeService;

    @PostMapping("/preview")
    public ResponseEntity<?> previewFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> schema = fileSchemaService.extractSchemaFromFile(file);
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate-from-file")
    public ResponseEntity<?> createSchemaFromFile(@RequestBody ApiSchemeRequest request, @AuthenticationPrincipal Jwt jwt ) {
        ApiSchemeResponse savedScheme = apiSchemeService.create(request, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedScheme);
    }

    @PostMapping("/generate-from-prompt")
    public ResponseEntity<?> generateFromPrompt(@RequestBody Map<String, String> payload) {
        try {
            String prompt = payload.get("prompt");
            if (prompt == null || prompt.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Prompt cannot be empty"));
            }

            Map<String, Object> aiDesignedSchema = fileSchemaService.generateSchemaFromPrompt(prompt);

            return ResponseEntity.ok(aiDesignedSchema);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
