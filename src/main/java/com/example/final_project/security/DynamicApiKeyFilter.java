package com.example.final_project.security;

import com.example.final_project.domain.ApiScheme;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DynamicApiKeyFilter extends OncePerRequestFilter {

    private final ApiSchemeRepository apiSchemeRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/engine-")) {
            try {
                String[] pathParts = path.split("/");



                if (pathParts.length >= 5) {
                    String enginePart = pathParts[3];
                    String slug = pathParts[4];

                    String projectKey = enginePart.replace("engine-", "");

                    System.out.println("DEBUG: ProjectKey=" + projectKey + " | Slug=" + slug);

                    ApiScheme scheme = apiSchemeRepository.findByProjectKeyAndSlug(projectKey, slug)
                            .orElse(null);

                    if (scheme != null) {
                        if (Boolean.TRUE.equals(scheme.getIsPublic())) {
                            filterChain.doFilter(request, response);
                            return;
                        }

                        String clientApiKey = request.getHeader("x-api-key");
                        if (clientApiKey != null && clientApiKey.equals(scheme.getApiKey())) {
                            filterChain.doFilter(request, response);
                            return;
                        }

                        handleError(response, "Unauthorized access", HttpStatus.UNAUTHORIZED);
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing URL: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
    private void handleError(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}