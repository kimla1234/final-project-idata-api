package com.example.final_project.features.apiEngine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/engine-{projectKey}/auth") // 🎯 កំណត់ Base Path នៅទីនេះរួចហើយ
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiEngineController {

    private final ApiEngineAuthService authService;

    // 🔗 URL ពិតប្រាកដនឹងក្លាយជា: /api/v1/engine-{projectKey}/auth/register
    @PostMapping("/register") // ✅ លុប /{projectKey}/auth ចេញ
    public ResponseEntity<?> register(@PathVariable String projectKey,
                                      @RequestHeader("x-api-key") String apiKey,
                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(authService.handleRegister(projectKey, apiKey, body));
    }

    // 🔗 URL ពិតប្រាកដនឹងក្លាយជា: /api/v1/engine-{projectKey}/auth/login
    @PostMapping("/login") // ✅ លុប /{projectKey}/auth ចេញ
    public ResponseEntity<?> login(@PathVariable String projectKey,
                                   @RequestHeader("x-api-key") String apiKey,
                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.handleLogin(projectKey, apiKey, body));
    }

    // 🔗 URL ពិតប្រាកដនឹងក្លាយជា: /api/v1/engine-{projectKey}/auth/refresh
    @PostMapping("/refresh") // ✅ លុប /{projectKey}/auth ចេញ
    public ResponseEntity<?> refresh(@PathVariable String projectKey,
                                     @RequestHeader("x-api-key") String apiKey,
                                     @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.handleRefreshToken(projectKey, apiKey, refreshToken));
    }
}