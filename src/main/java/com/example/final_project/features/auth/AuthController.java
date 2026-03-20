package com.example.final_project.features.auth;

import com.example.final_project.base.BaseMessage;
import com.example.final_project.features.auth.dto.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
@Hidden
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseMessage register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException {

        return authService.register(registerRequest);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public BaseMessage verifyAccount(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest) {

        return authService.verifyUserAccount(verifyCodeRequest);
    }

    @PostMapping("/user/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse userLogin(@Valid @RequestBody LoginRequest loginRequest) {

        return authService.userLogin(loginRequest);
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        return authService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/{email}/send-verify-code")
    public BaseMessage sendVerifyCode(@PathVariable String email) throws MessagingException {

        return authService.sendVerifyCode(email);
    }

    @PostMapping("/reset-token")
    public ResetTokenResponse getResetToken(@Valid @RequestBody ResetTokenRequest resetTokenRequest) {

        return authService.getResetToken(resetTokenRequest);
    }

    @PostMapping("/reset-password")
    public BaseMessage resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {

        return authService.resetPassword(resetPasswordRequest);
    }




}
