package com.example.final_project.features.woekspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceCreateRequest(
        @NotBlank(message = "ឈ្មោះ Workspace មិនអាចទទេបានទេ")
        @Size(max = 100, message = "ឈ្មោះ Workspace មិនអាចលើសពី ១០០ អក្សរបានទេ")
        String name,

        @Size(max = 255, message = "ការពណ៌នា (Description) មិនអាចលើសពី ២៥៥ អក្សរបានទេ")
        String description
) {
}
