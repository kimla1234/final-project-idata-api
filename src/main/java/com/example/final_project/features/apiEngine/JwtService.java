package com.example.final_project.features.apiEngine;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret:your-very-secure-secret-key-should-be-long-enough}")
    private String secretKey;

    // សុពលភាព Access Token (ឧទាហរណ៍៖ ១ ម៉ោង)
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;
    // សុពលភាព Refresh Token (ឧទាហរណ៍៖ ៧ ថ្ងៃ)
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    public String generateAccessToken(String email, String projectKey) {
        return buildToken(email, projectKey, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // ១ ថ្ងៃ
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 🎯 កែពី getSignInKey មក getSigningKey វិញ
                .compact();
    }
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION)) // ៧ ថ្ងៃ
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String buildToken(String email, String projectKey, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("projectKey", projectKey); // 🎯 ដាក់ ProjectKey ក្នុង Token ដើម្បីដឹងថាជារបស់ App ណា

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 🎯 ១. សម្រាប់ឆែកមើលថា Token ត្រឹមត្រូវ និងមិនទាន់ផុតកំណត់ (Expired)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // បើ Token ខុស format ឬ Expired វានឹងធ្លាក់ចូលទីនេះ
            return false;
        }
    }

    // 🎯 ២. សម្រាប់ទាញយក Claims ផ្សេងៗ (ដូចជា projectKey) ចេញពី Token
    public String extractProjectKey(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("projectKey", String.class);
    }
}