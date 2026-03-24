package com.example.final_project.features.ApiData;

import com.example.final_project.domain.ApiScheme;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MockDataService {
    private final DynamicDataService dynamicDataService;
    private final WebClient.Builder webClientBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String modelName;


    public void generateAndSave(ApiScheme schema, String instruction) throws JsonProcessingException {
        String safeInstruction = (instruction == null) ? "Generate realistic data" : instruction;
        String schemaJson = objectMapper.writeValueAsString(schema.getProperties());

        String promptText = "Act as a specialized JSON generator. Generate a JSON array of 5 realistic objects.\n" +
                "STRICT RULES:\n" +
                "1. MATCH these properties and data types EXACTLY: " + schemaJson + "\n" +
                "2. If a field type is 'string', the value MUST be a string.\n" +
                "3. If a field type is 'number', the value MUST be a number.\n" +
                "4. Context: " + safeInstruction + "\n" +
                "5. Output ONLY the raw JSON array. No markdown, no explanations.\n" +
                "6. DO NOT include the 'id' field.";

        Map<String, Object> payload = Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a precise JSON data generator."),
                        Map.of("role", "user", "content", promptText)
                ),
                "temperature", 0.7
        );

        try {
            String aiResponse = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://api.idata.fit") // តម្រូវការរបស់ OpenRouter
                    .header("X-Title", "API Engine Project")        // តម្រូវការរបស់ OpenRouter
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class).flatMap(error ->
                                    Mono.error(new RuntimeException("OpenRouter API Error: " + error)))
                    )
                    .bodyToMono(String.class)
                    .block();

            if (aiResponse == null || aiResponse.isBlank()) {
                throw new RuntimeException("AI returned an empty response.");
            }

            List<Map<String, Object>> mockData = parseAiResponse(aiResponse);

            insertMockDataSafely(schema, mockData);

        } catch (Exception e) {
            System.err.println("❌ ERROR LOG: " + e.getMessage());
            throw new RuntimeException("AI Generation Failed: " + e.getMessage());
        }
    }

    private void insertMockDataSafely(ApiScheme schema, List<Map<String, Object>> mockData) {
        String findMaxSql = "SELECT COALESCE(MAX((content->>'id')::int), 0) FROM api_data WHERE api_scheme_id = ?";
        Integer maxId = jdbcTemplate.queryForObject(findMaxSql, Integer.class, schema.getId());
        int nextId = (maxId == null ? 0 : maxId) + 1;

        for (Map<String, Object> item : mockData) {
            item.put("id", nextId++);
        }
        dynamicDataService.insertBulk("api_data", mockData, schema.getId());
    }

    private List<Map<String, Object>> parseAiResponse(String aiResponse) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);

            // OpenRouter/OpenAI format: choices[0].message.content
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new RuntimeException("AI Response has no choices. Full Response: " + aiResponse);
            }

            String jsonContent = choices.get(0)
                    .path("message")
                    .path("content").asText();

            if (jsonContent == null || jsonContent.isEmpty()) {
                throw new RuntimeException("AI returned empty content");
            }

            jsonContent = jsonContent.trim();
            if (jsonContent.startsWith("```")) {
                jsonContent = jsonContent.replaceAll("^```[a-z]*\\n|\\n```$", "");
            }

            return objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Parsing Failed: " + e.getMessage() + " | Raw AI Response: " + aiResponse);
        }
    }
}