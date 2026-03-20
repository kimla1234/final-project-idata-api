package com.example.final_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("User-API-Engine")
                // ប្តូរ "com.example.final_project.features.apiScheme" ទៅតាម Package របស់បង
                .packagesToScan("com.example.final_project.features.apiScheme")
                .pathsToMatch("/api/v1/engine/**")
                .build();
    }
}