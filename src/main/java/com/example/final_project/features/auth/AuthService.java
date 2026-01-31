package com.example.final_project.features.auth;

import com.example.final_project.base.BaseMessage;
import com.example.final_project.features.auth.dto.*;
import jakarta.mail.MessagingException;

public interface AuthService {
    BaseMessage register(RegisterRequest registerRequest) throws MessagingException;
    BaseMessage verifyUserAccount(VerifyCodeRequest verifyCodeRequest);
    AuthResponse userLogin(LoginRequest loginRequest);
    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    BaseMessage sendVerifyCode(String email) throws MessagingException;
    ResetTokenResponse getResetToken(ResetTokenRequest resetTokenRequest);
    BaseMessage resetPassword(ResetPasswordRequest resetPasswordRequest);


}
