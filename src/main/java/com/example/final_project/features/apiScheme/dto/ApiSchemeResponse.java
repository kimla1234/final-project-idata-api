package com.example.final_project.features.apiScheme.dto;

import com.example.final_project.features.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiSchemeResponse(
        Integer id,
        String name,
        String endpointUrl,
        String description,

        // ប្តូរមកប្រើ List នៃ Map ដើម្បីផ្ទុកលម្អិតដូចក្នុង UI (FieldName, Type, Required)
        List<Map<String, Object>> properties,

        // បន្ថែម Keys ដើម្បីបង្ហាញ Primary Key និង Foreign Key ក្នុង UI
        List<Map<String, Object>> keys,

        Boolean isPublic,

        // ព័ត៌មានអំពីម្ចាស់ និងទីតាំង
        // 🎯 បន្ថែម Field ទាំងនេះ
        String ownerName,
        String ownerAvatar, // សម្រាប់ URL រូបភាព
        String ownerHandle, // សម្រាប់ @username

        Integer folderId,
        String folderName,
        Integer workspaceId,
        String ownerUuid,

        // ស្ថិតិសម្រាប់ Community (Behance Style)
        Integer forkCount,
        Integer viewCount,
        Integer totalRequests, // ទាញចេញពី ProjectAnalytics

        // ព័ត៌មានអំពីប្រភព (សម្រាប់ករណី Fork)
        Integer parentId,
        String parentName,
        Boolean isPublished,
        Boolean isFollowed,


        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        UserResponse lastModifiedBy,

        Long dataCount,

        String apiKey
) {
        // បង្កើត Helper Method ដើម្បីប្តូរតម្លៃ isFollowed
        public ApiSchemeResponse withIsFollowed(Boolean followedStatus) {
                return new ApiSchemeResponse(
                        id, name, endpointUrl, description, properties, keys, isPublic,
                        ownerName, ownerAvatar, ownerHandle, folderId, folderName,
                        workspaceId, ownerUuid, forkCount, viewCount, totalRequests,
                        parentId, parentName, isPublished,
                        followedStatus, // 🎯 ដាក់តម្លៃថ្មីនៅទីនេះ
                        createdAt, updatedAt, lastModifiedBy, dataCount, apiKey
                );
        }
}