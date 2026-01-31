package com.example.final_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${media.server-path}")
    private String serverPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This maps http://localhost:8081/media/IMAGE/your-file.jpg
        // to the physical folder on your computer
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + serverPath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // ដាក់ឈ្មោះ Domain ឱ្យចំ
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true); // បើគ្មានបន្ទាត់នេះទេ នឹងជាប់ CORS និង 401 រហូត
    }
}
