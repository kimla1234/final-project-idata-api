package com.example.final_project.features.ApiData;

import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiSchemeRepository apiSchemeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🎯 ១. ឆែកតែផ្លូវណាដែលចាប់ផ្ដើមដោយ /api/v1/engine-
        if (path.startsWith("/api/v1/engine-")) {

            String[] parts = path.split("/");

            // URL: /api/v1/engine-p-d9eba188/staff
            // parts[0] = ""
            // parts[1] = "api"
            // parts[2] = "v1"
            // parts[3] = "engine-p-d9eba188"
            // parts[4] = "staff"

            if (parts.length < 5) {
                filterChain.doFilter(request, response);
                return;
            }

            // 🎯 ២. ទាញយក projectKey (កាត់ពាក្យ engine- ចេញ)
            String projectKey = parts[3].replace("engine-", "");

            // 🎯 ៣. ទាញយក slug (staff)
            String slug = parts[4].toLowerCase();

            // 🔍 បន្ថែម Log នេះដើម្បីមើលក្នុង IntelliJ Console ពេលបង Execute ក្នុង Swagger
            System.out.println("DEBUG Filter: ProjectKey=[" + projectKey + "], Slug=[" + slug + "]");

            // ៤. រកមើល Schema ក្នុង Database
            // ត្រូវប្រាកដថា findByProjectKeyAndSlug ក្នុង Repo របស់បងដើរត្រឹមត្រូវ
            var schemeOpt = apiSchemeRepository.findByProjectKeyAndSlug(projectKey, slug);

            if (schemeOpt.isPresent()) {
                var scheme = schemeOpt.get();
                System.out.println("DEBUG Filter: Found Schema! isPublic=" + scheme.getIsPublic());

                // ៥. បើជា Public ឱ្យឆ្លងផុត (ទោះគ្មាន Header x-api-key ក៏ដោយ)
                if (Boolean.TRUE.equals(scheme.getIsPublic())) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // ៦. បើជា Private ត្រូវឆែក x-api-key ក្នុង Header
                String providedKey = request.getHeader("x-api-key");

                if (providedKey != null && providedKey.equals(scheme.getApiKey())) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // ៧. បើអត់មាន Key ឬ Key ខុស បោះ Error 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Unauthorized: API នេះជា Private សូមបញ្ចូល x-api-key ឱ្យបានត្រឹមត្រូវ");
                return;
            } else {
                // 🎯 ករណីរកក្នុង DB មិនឃើញ (ដែលបងជាប់ 404 មិញ)
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("រកមិនឃើញ API សម្រាប់ Project: " + projectKey + " និង Slug: " + slug);
                return;
            }
        }

        // បើមិនមែនជាផ្លូវ Engine API ទេ ឱ្យវាទៅ Security ធម្មតា
        filterChain.doFilter(request, response);
    }
}