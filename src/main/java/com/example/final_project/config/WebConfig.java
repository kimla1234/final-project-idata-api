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

       // registry.addMapping("/**")
                // 🎯 ប្រើ patterns ជំនួស origins ធម្មតា ដើម្បីដោះស្រាយ Error 500
       //         .allowedOriginPatterns("*")

                // 🎯 បញ្ជាក់ Header ឱ្យចំ ជាពិសេស x-api-key
       //         .allowedHeaders("Authorization", "Content-Type", "x-api-key", "Accept", "Origin")

                // កំណត់ Method ឱ្យច្បាស់លាស់ (ដក .allowedMethods("*") ចេញដើម្បីកុំឱ្យជាន់គ្នា)
        //        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

         //       .allowCredentials(true)
         //       .maxAge(3600);
    }
}
