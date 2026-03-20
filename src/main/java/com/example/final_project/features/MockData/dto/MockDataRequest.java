package com.example.final_project.features.MockData.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MockDataRequest(
        @NotNull(message = "ទិន្នន័យ JSON មិនអាចទទេបានទេ")
        Map<String, Object> data // ឧទាហរណ៍៖ {"name": "Product A", "price": 100}
) {}