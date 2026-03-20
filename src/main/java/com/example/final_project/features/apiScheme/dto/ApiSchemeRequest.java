package com.example.final_project.features.apiScheme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record ApiSchemeRequest(
        @NotBlank(message = "ឈ្មោះ API មិនអាចទទេបានទេ")
        String name,

        @NotBlank(message = "URL Endpoint មិនអាចទទេបានទេ")
        String endpointUrl,

        String description,

        /**
         * ប្ដូរពី Map មកជា List វិញ ដើម្បីផ្ទុក Detail ដូចក្នុង UI:
         * [ {"fieldName": "id", "type": "string", "isRequired": true}, ... ]
         */
        @NotNull(message = "សូមកំណត់រចនាសម្ព័ន្ធ API របស់អ្នក")
        List<Map<String, Object>> properties,

        /**
         * សម្រាប់ផ្ទុកព័ត៌មាន Keys ពី UI:
         * [ {"columnName": "id", "isPk": true, "isFk": false, "refTable": ""} ]
         */
        List<Map<String, Object>> keys,

        Boolean isPublic,

        @NotNull(message = "សូមជ្រើសរើស Folder")
        Integer folderId
) {
}
