package com.example.final_project.features.apiScheme;

import com.example.final_project.domain.ApiData;
import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.ApiData.ApiDataRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@RequestMapping("/api/v1/engine-{projectKey}/{slug}")
@RequestMapping("/api/v1/engine-{projectKey}/{slug:(?!auth$).*}")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Dynamic Engine", description = "Public/Private API Engine")
public class DynamicApiEngineController {

    private final ApiSchemeRepository apiSchemeRepository;
    private final ApiSchemeService apiSchemeService;
    private final ApiDataRepository apiDataRepository;



    @GetMapping
    public ResponseEntity<?> handleGet(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("slug") String slug) {

        ApiScheme scheme = findScheme(projectKey, slug);
        List<ApiData> allData = apiDataRepository.findAllByApiSchemeId(scheme.getId());

        return ResponseEntity.ok(allData.stream().map(this::mapToResponse).toList());
    }

    @PostMapping
    public ResponseEntity<?> handlePost(
            @PathVariable String projectKey,
            @PathVariable String slug,
            @RequestBody Map<String, Object> body) {

        ApiScheme scheme = findScheme(projectKey, slug);

        String pkName = "id";

        List<ApiData> existingData = apiDataRepository.findAllByApiSchemeId(scheme.getId());

        long nextId = existingData.stream()
                .map(data -> {
                    Object val = data.getContent().get(pkName);
                    if (val instanceof Number) return ((Number) val).longValue();
                    if (val instanceof String) {
                        try { return Long.parseLong((String) val); } catch (Exception e) { return 0L; }
                    }
                    return 0L;
                })
                .max(Long::compare)
                .orElse(0L) + 1;

        Map<String, Object> contentWithId = new HashMap<>(body);
        contentWithId.put(pkName, nextId);

        ApiData apiData = new ApiData();
        apiData.setApiScheme(scheme);
        apiData.setContent(contentWithId);
        ApiData saved = apiDataRepository.save(apiData);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> handleGetById(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("slug") String slug,
            @PathVariable("id") String id) {

        ApiScheme scheme = findScheme(projectKey, slug);

        ApiData data = apiDataRepository.findByJsonId(scheme.getId().longValue(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទិន្នន័យ ID: " + id));

        return ResponseEntity.ok(mapToResponse(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> handleUpdate(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("slug") String slug,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body) {

        ApiScheme scheme = findScheme(projectKey, slug);
        ApiData existingData = apiDataRepository.findByJsonId(scheme.getId().longValue(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទិន្នន័យដើម្បី Update ID: " + id));

        existingData.setContent(body);
        apiDataRepository.save(existingData);

        return ResponseEntity.ok(mapToResponse(existingData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> handleDelete(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("slug") String slug,
            @PathVariable("id") String id) {

        ApiScheme scheme = findScheme(projectKey, slug);
        ApiData data = apiDataRepository.findByJsonId(scheme.getId().longValue(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទិន្នន័យដើម្បីលុប ID: " + id));

        apiDataRepository.delete(data);
        return ResponseEntity.noContent().build();
    }

    // --- Helper Methods ---

    private ApiScheme findScheme(String projectKey, String slug) {
        return apiSchemeRepository.findByProjectKeyAndSlug(projectKey, slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "រកមិនឃើញ API សម្រាប់ Project: " + projectKey + " និង Slug: " + slug));
    }

    private Map<String, Object> mapToResponse(ApiData data) {
        Map<String, Object> res = new HashMap<>(data.getContent());
        if (!res.containsKey("id")) {
            res.put("id", data.getId());
        }
        return res;
    }
}