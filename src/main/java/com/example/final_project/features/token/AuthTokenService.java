package com.example.final_project.features.token;

import com.example.final_project.base.BaseMessage;
import com.example.final_project.domain.Token;
import com.example.final_project.domain.User;
import com.example.final_project.features.auth.dto.AuthResponse;
import com.example.final_project.features.auth.dto.RefreshTokenRequest;
import org.springframework.security.core.Authentication;

public interface AuthTokenService {
    AuthResponse createToken(Authentication authentication);

    String createAccessToken(Authentication authentication);

    String createRefreshToken(Authentication authentication);

    String generateResetToken(String email, User user, Token token);

    BaseMessage resetPassword(String token, String newPassword);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
