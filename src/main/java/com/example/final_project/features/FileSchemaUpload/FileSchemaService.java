package com.example.final_project.features.FileSchemaUpload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileSchemaService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String modelName;

    /**
     * 🎯 មុខងារចម្បង៖ អាន File (JSON/CSV) និងឱ្យ AI វិភាគរក Schema
     */
    public Map<String, Object> extractSchemaFromFile(MultipartFile file) {
        try {
            String fileContent = "";
            String contentType = file.getContentType();

            if (contentType != null && contentType.contains("json")) {
                fileContent = readJsonPreview(file);
            } else if (contentType != null && (contentType.contains("csv") || contentType.contains("text"))) {
                fileContent = readCsvPreview(file);
            } else {
                throw new RuntimeException("Unsupported file type (Only JSON & CSV)");
            }

            String systemPrompt = "Analyze this data and return ONLY a JSON object with: name, description, properties[{fieldName, type, required}], keys[]. Data: ";
            return callAiToInferSchema(systemPrompt + fileContent);

        } catch (Exception e) {
            throw new RuntimeException("Schema Extraction Failed: " + e.getMessage());
        }
    }

    private String readJsonPreview(MultipartFile file) throws Exception {
        JsonNode root = objectMapper.readTree(file.getInputStream());
        // បើយក Array មក preview យកតែ object ទី១ បានហើយដើម្បីសន្សំ Token
        return root.isArray() ? root.get(0).toString() : root.toString();
    }

    private String readCsvPreview(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // អានត្រឹម ៥ ជួរដើម្បីឱ្យ AI ស្គាល់ Header និងទម្រង់ទិន្នន័យ
            return reader.lines().limit(5).collect(Collectors.joining("\n"));
        }
    }

    private Map<String, Object> callAiToInferSchema(String fullPrompt) {
        // 🎯 បង្កើត Payload តាមទម្រង់ OpenRouter (OpenAI Compatible)
        Map<String, Object> payload = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a Data Architect. Return ONLY raw JSON."),
                        Map.of("role", "user", "content", fullPrompt)
                ),
                "temperature", 0.3 // កំណត់ទាបដើម្បីឱ្យលទ្ធផលច្បាស់លាស់ (Strict JSON)
        );

        try {
            String aiResponse = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8081")
                    .header("X-Title", "API Schema Generator")
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class).flatMap(error ->
                                    Mono.error(new RuntimeException("OpenRouter API Error: " + error)))
                    )
                    .bodyToMono(String.class)
                    .block();

            return parseAiSchemaResponse(aiResponse);
        } catch (Exception e) {
            throw new RuntimeException("AI Error: " + e.getMessage());
        }
    }

    private Map<String, Object> parseAiSchemaResponse(String aiResponse) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);

            // 🎯 ទាញយក content តាមទម្រង់ choices[0].message.content
            String jsonContent = root.path("choices").get(0)
                    .path("message").path("content").asText();

            if (jsonContent == null || jsonContent.isEmpty()) {
                throw new RuntimeException("AI returned empty content");
            }

            // សម្អាត Markdown បើមាន
            jsonContent = jsonContent.trim().replaceAll("^```json|```$", "");

            return objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("AI JSON Parsing Failed: " + e.getMessage() + " | Raw: " + aiResponse);
        }
    }

    /**
     * 🎯 បង្កើត Schema តាមរយៈការសរសេរ Prompt ផ្ទាល់មាត់
     */
    public Map<String, Object> generateSchemaFromPrompt(String userPrompt) {
        try {
            String systemPrompt = """
                Design an API schema. Return a JSON object with:
                1. "name": String
                2. "endpointUrl": String (kebab-case)
                3. "description": String
                4. "properties": Array of { "fieldName": string, "type": string, "required": boolean }
                5. "keys": Array of { "columnName": string, "primaryKey": boolean, "foreignKey": boolean, "referenceTable": string }
                
                Rules: Use camelCase for fieldNames. If ID, type is 'number'.
                """;

            return callAiToInferSchema(systemPrompt + "\nUser Request: " + userPrompt);
        } catch (Exception e) {
            throw new RuntimeException("AI Generation via Prompt failed: " + e.getMessage());
        }
    }
}