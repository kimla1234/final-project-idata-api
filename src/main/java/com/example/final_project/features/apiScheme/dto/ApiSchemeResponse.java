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

        List<Map<String, Object>> properties,

        List<Map<String, Object>> keys,

        Boolean isPublic,

        String ownerName,
        String ownerAvatar,
        String ownerHandle,

        Integer folderId,
        String folderName,
        Integer workspaceId,
        String ownerUuid,

        Integer forkCount,
        Integer viewCount,
        Integer totalRequests,

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
        public ApiSchemeResponse withIsFollowed(Boolean followedStatus) {
                return new ApiSchemeResponse(
                        id, name, endpointUrl, description, properties, keys, isPublic,
                        ownerName, ownerAvatar, ownerHandle, folderId, folderName,
                        workspaceId, ownerUuid, forkCount, viewCount, totalRequests,
                        parentId, parentName, isPublished,
                        followedStatus,
                        createdAt, updatedAt, lastModifiedBy, dataCount, apiKey
                );
        }
}