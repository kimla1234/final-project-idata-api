package com.example.final_project.features.ApiData;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicDataService {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void insertBulk(String tableName, List<Map<String, Object>> dataList, Integer apiSchemeId) {
        if (dataList == null || dataList.isEmpty()) return;

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> columnNames = dataList.get(0).keySet().stream()
                .filter(key -> !key.equalsIgnoreCase("id"))
                .collect(Collectors.toList());

        String sql = "INSERT INTO api_data (content, api_scheme_id, created_at) VALUES (?::jsonb, ?, NOW())";

        List<Object[]> batchArgs = dataList.stream()
                .map(row -> {
                    try {
                        String jsonContent = objectMapper.writeValueAsString(row);
                        return new Object[]{ jsonContent, apiSchemeId };
                    } catch (Exception e) {
                        throw new RuntimeException("JSON Conversion Failed");
                    }
                })
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}