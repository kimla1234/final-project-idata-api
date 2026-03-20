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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiEngineAuthService {

    private final ApiSchemeRepository apiSchemeRepository;
    private final ApiDataRepository apiDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // 🎯 Service សម្រាប់បង្កើត JWT តាម standard របស់ប
    // ង

    // 🎯 ១. REGISTER: រក្សាទុក User ថ្មីចូលក្នុង Project របស់អតិថិជន
    @Transactional
    public Map<String, Object> handleRegister(String projectKey, String apiKey, Map<String, Object> userData) {
        ApiScheme authScheme = validateKeys(projectKey, apiKey);

        // Hash Password មុនរក្សាទុក
        if (userData.containsKey("password")) {
            userData.put("password", passwordEncoder.encode(userData.get("password").toString()));
        }

        ApiData newEndUser = new ApiData();
        newEndUser.setApiScheme(authScheme);
        newEndUser.setJsonData(userData); // ទុកក្នុង JSON Column
        newEndUser.setCreatedAt(LocalDateTime.now());

        apiDataRepository.save(newEndUser);
        return Map.of("message", "User registered successfully", "status", 201);
    }

    public Map<String, Object> handleLogin(String projectKey, String apiKey, Map<String, String> body) {
        // 🎯 ១. ផ្ទៀងផ្ទាត់ Project Key និង API Key ដើម្បីប្រាកដថាអតិថិជនមានសិទ្ធិប្រើប្រាស់ Auth នេះ
        ApiScheme scheme = apiSchemeRepository.findByProjectKeyAndSlug(projectKey, "auth")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auth scheme not found for this project"));

        // 🎯 ២. ចាប់យកទិន្នន័យពី Request Body
        String username = body.get("username");
        String rawPassword = body.get("password");

        if (username == null || rawPassword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        // 🎯 ៣. ស្វែងរកទិន្នន័យអ្នកប្រើប្រាស់ (End-user) នៅក្នុង Table api_data តាមរយៈ scheme_id
        List<ApiData> allData = apiDataRepository.findBySchemeId(scheme.getId().intValue());

        // ច្រោះយកតែ User ណាដែលមាន username ត្រូវគ្នា (រកក្នុង JSONB Column)
        ApiData userRecord = allData.stream()
                .filter(d -> d.getJsonData() != null && username.equals(d.getJsonData().get("username")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // 🎯 ៤. ផ្ទៀងផ្ទាត់ Password ដោយប្រើ matches()
        // (ព្រោះ storedPassword ក្នុង DB គឺជា Hash string ដែលបានមកពី BCrypt)
        String storedPassword = (String) userRecord.getJsonData().get("password");

        if (storedPassword == null || !passwordEncoder.matches(rawPassword, storedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        // 🎯 ៥. បង្កើត JWT Token ពិតប្រាកដ
        // បងអាចបន្ថែមព័ត៌មានផ្សេងៗចូលក្នុង Claims បានតាមចិត្ត
        String accessToken = jwtService.generateToken(username); // ឬ generateAccessToken(username, projectKey)
        String refreshToken = jwtService.generateRefreshToken(username);

        // លុប password ចេញពីទិន្នន័យដែលត្រូវផ្ញើទៅឱ្យ client ដើម្បីសុវត្ថិភាព
        Map<String, Object> userResponse = new java.util.HashMap<>(userRecord.getJsonData());
        userResponse.remove("password");

        return Map.of(
                "message", "Login successful",
                "status", 200,
                "accessToken", accessToken,  // 🎯 ប្តូរឈ្មោះឱ្យច្បាស់
                "refreshToken", refreshToken, // 🎯 បញ្ជូន Refresh Token ទៅវិញ
                "user", userResponse
        );
    }
    private ApiScheme validateKeys(String pKey, String aKey) {
        return apiSchemeRepository.findByWorkspaceProjectKeyAndApiKey(pKey, aKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Project or API Key"));

}

    public Map<String, Object> handleRefreshToken(String projectKey, String apiKey, String refreshToken) {
        // ១. ផ្ទៀងផ្ទាត់ Keys
        validateKeys(projectKey, apiKey);

        // ២. ផ្ទៀងផ្ទាត់ Refresh Token តាមរយៈ JwtService
        // (បងត្រូវថែម method extractEmail ក្នុង JwtService ផង)
        String email = jwtService.extractEmail(refreshToken);

        // ៣. បង្កើត Access Token ថ្មី
        String newAccessToken = jwtService.generateAccessToken(email, projectKey);

        return Map.of(
                "accessToken", newAccessToken,
                "tokenType", "Bearer"
        );
    }
}