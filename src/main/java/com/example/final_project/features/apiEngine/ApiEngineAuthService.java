package com.example.final_project.features.apiEngine;

import com.example.final_project.domain.ApiData;
import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.ApiData.ApiDataRepository;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ApiEngineAuthService {

    private final ApiSchemeRepository apiSchemeRepository;
    private final ApiDataRepository apiDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Service for generating JWT based on project standards

    /**
     * REGISTER: Persists a new end-user into the customer's specific project data.
     */
    @Transactional
    public Map<String, Object> handleRegister(String projectKey, String apiKey, Map<String, Object> userData) {
        ApiScheme authScheme = validateKeys(projectKey, apiKey);

        // Hash the password before saving
        if (userData.containsKey("password")) {
            userData.put("password", passwordEncoder.encode(userData.get("password").toString()));
        }

        ApiData newEndUser = new ApiData();
        newEndUser.setApiScheme(authScheme);
        newEndUser.setJsonData(userData); // Stored in the JSONB Column
        newEndUser.setCreatedAt(LocalDateTime.now());

        apiDataRepository.save(newEndUser);
        return Map.of("message", "User registered successfully", "status", 201);
    }

    /**
     * LOGIN: Authenticates end-users and generates JWT tokens.
     */
    public Map<String, Object> handleLogin(String projectKey, String apiKey, Map<String, String> body) {
        // 1. Validate Project Key and API Key to ensure the client is authorized for this Auth scheme
        ApiScheme scheme = apiSchemeRepository.findByProjectKeyAndSlug(projectKey, "auth")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auth scheme not found for this project"));

        // 2. Extract data from the Request Body
        String username = body.get("username");
        String rawPassword = body.get("password");

        if (username == null || rawPassword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        // 3. Search for the end-user data in the api_data table via scheme_id
        List<ApiData> allData = apiDataRepository.findBySchemeId(scheme.getId().intValue());

        // Filter for the user with the matching username (searching inside the JSONB Column)
        ApiData userRecord = allData.stream()
                .filter(d -> d.getJsonData() != null && username.equals(d.getJsonData().get("username")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // 4. Verify the password using matches()
        // (The storedPassword in DB is a BCrypt hashed string)
        String storedPassword = (String) userRecord.getJsonData().get("password");

        if (storedPassword == null || !passwordEncoder.matches(rawPassword, storedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        // 5. Generate a standard JWT Access Token and Refresh Token
        // You can add additional claims if needed
        String accessToken = jwtService.generateToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        // Remove the password from the response data for security purposes
        Map<String, Object> userResponse = new HashMap<>(userRecord.getJsonData());
        userResponse.remove("password");

        return Map.of(
                "message", "Login successful",
                "status", 200,
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", userResponse
        );
    }

    /**
     * Internal utility to validate workspace project and API keys.
     */
    private ApiScheme validateKeys(String pKey, String aKey) {
        return apiSchemeRepository.findByWorkspaceProjectKeyAndApiKey(pKey, aKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Project or API Key"));
    }

    /**
     * REFRESH TOKEN: Generates a new Access Token using a valid Refresh Token.
     */
    public Map<String, Object> handleRefreshToken(String projectKey, String apiKey, String refreshToken) {
        // 1. Validate Keys
        validateKeys(projectKey, apiKey);

        // 2. Validate the Refresh Token via JwtService (requires extractEmail method in JwtService)
        String email = jwtService.extractEmail(refreshToken);

        // 3. Generate a fresh Access Token
        String newAccessToken = jwtService.generateAccessToken(email, projectKey);

        return Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer"
        );
    }
}