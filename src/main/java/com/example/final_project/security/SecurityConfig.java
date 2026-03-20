package com.example.final_project.security;

import com.example.final_project.utils.KeyUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final KeyUtil keyUtil;
    private final DynamicApiKeyFilter dynamicApiKeyFilter;




    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // ប្តូរពីការប្រើ Constructor មកប្រើ Setter បែបនេះវិញដើម្បីបាត់ Error
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // Keep this for AuthTokenServiceImpl, but it won't be used by the FilterChain automatically
    @Bean
    JwtAuthenticationProvider jwtAuthenticationProvider(@Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(refreshJwtDecoder);
        return provider;
    }

    // ១. ថែម Bean នេះចូលក្នុង SecurityConfig
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🎯 ប្រើ patterns ជំនួស origins ធម្មតា ដើម្បីដោះស្រាយ Error 500
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));

        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));

        // 🎯 ត្រូវតែមាន "x-api-key" ក្នុងជួរនេះ
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "x-api-key", "Accept", "Origin"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
                                                   throws Exception {

        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // 🎯 ចំណុចសំខាន់៖ ដាក់ ApiKeyFilter ឱ្យដើរមុន AuthorizationFilter
                .addFilterBefore(dynamicApiKeyFilter, AuthorizationFilter.class)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/engine-*/**").permitAll()
                        .requestMatchers("/api/v1/engine/**").permitAll()
                        .requestMatchers("/api/v1/engine-**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        //.requestMatchers(HttpMethod.GET,"/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/users/me").authenticated() // OR if you use roles:

                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/media/**").permitAll()
                        .requestMatchers("/media/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/api-schemes/public/**").permitAll()

                        // កែពី hasAnyAuthority មក authenticated() វិញ
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/v1/users/*/admins/**").hasAuthority("SCOPE_admin:read")

                        .requestMatchers(HttpMethod.POST, "/v1/search/dataImport").hasAuthority("SCOPE_admin:write")
                        .requestMatchers("/v1/media/**").hasAnyAuthority("SCOPE_admin:write", "SCOPE_user:write")
                        .requestMatchers("/v1/video/**").permitAll()

                        .anyRequest().authenticated()
                )
        .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                // បន្ថែមចំណុចនេះ ប្រសិនបើបងចង់ឱ្យវា Ignore ផ្លូវខ្លះ (Optional)
        );
        // enable jwt security
        httpSecurity.oauth2ResourceServer(jwt -> jwt
                .jwt(Customizer.withDefaults()));

        // disable csrf for submit form because we develop api
        httpSecurity.csrf(token -> token.disable());

        // change from statefull to stateless because api use stateless
        httpSecurity.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }

    // --- Access Token Beans ---

    @Primary
    @Bean("jwkSource")
    JWKSource<SecurityContext> jwkSource(KeyUtil keyUtil) {

        RSAKey rsaKey = new RSAKey.Builder(keyUtil.getAccessTokenPublicKey())
                .keyID(UUID.randomUUID().toString())
                .privateKey(keyUtil.getAccessTokenPrivateKey())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return ((jwkSelector, securityContext) -> jwkSelector
                .select(jwkSet));
    }


    // issue access token or get access token
    @Primary
    @Bean("jwtEncoder")
    JwtEncoder jwtEncoder(@Qualifier("jwkSource") JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }


    // when client submit token from header to get the protected resource
    @Primary
    @Bean("jwtDecoder")
    JwtDecoder jwtDecoder(KeyUtil keyUtil) throws JOSEException {
        return NimbusJwtDecoder
                .withPublicKey(keyUtil.getAccessTokenPublicKey())
                .build();
    }

    @Bean("refreshJwkSource")
    JWKSource<SecurityContext> refreshJwkSource() {

        RSAKey rsaKey = new RSAKey.Builder(keyUtil.getRefreshTokenPublicKey())
                .keyID(UUID.randomUUID().toString())
                .privateKey(keyUtil.getRefreshTokenPrivateKey())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return ((jwkSelector, securityContext) -> jwkSelector
                .select(jwkSet));
    }

    // issue access token or get access token
    @Bean("refreshJwtEncoder")
    JwtEncoder refreshJwtEncoder(@Qualifier("refreshJwkSource") JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }


    // when client submit token from header or body to get the protected resource
    @Bean("refreshJwtDecoder")
    JwtDecoder refreshJwtDecoder() {
        return NimbusJwtDecoder
                .withPublicKey(keyUtil.getRefreshTokenPublicKey())
                .build();
    }
}