package com.example.final_project.features.auth;

import com.example.final_project.base.BaseMessage;
import com.example.final_project.domain.*;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import com.example.final_project.features.auth.dto.*;
import com.example.final_project.features.folder.FolderRepository;
import com.example.final_project.features.token.AuthTokenService;
import com.example.final_project.features.token.TokenRepository;
import com.example.final_project.features.user.RoleRepository;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.features.user.UserService;
import com.example.final_project.features.woekspace.InvitationRepository;
import com.example.final_project.features.woekspace.WorkspaceMemberRepository;
import com.example.final_project.features.woekspace.WorkspaceRepository;
import com.example.final_project.mapper.AuthMapper;
import com.example.final_project.utils.GenerateNumberUtil;
import com.example.final_project.utils.PasswordValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final AuthRepository authRepository;
    private final TokenRepository tokenRepository;
    // 1. Uncomment this
    private final DaoAuthenticationProvider daoAuthenticationProvider;

    // 2. Add this (it's used in userLogin but was missing)
    private final AuthTokenService authTokenService;
    private final UserService userService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final InvitationRepository invitationRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final ApiSchemeRepository apiSchemeRepository;
    @Value("${spring.mail.username}")
    private String adminMail;

    @Transactional
    @Override
    public BaseMessage register(RegisterRequest registerRequest) throws MessagingException {

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (!registerRequest.password().equals(registerRequest.confirmPassword())) {
            return BaseMessage.builder().message("Password and Confirm Password must be same").build();
        }

        if (!PasswordValidator.validate(registerRequest.password())) {
            return BaseMessage.builder().message("Password must contain at least 8 characters, 1 uppercase, 1 lowercase, 1 number and 1 special character").build();
        }

        String verifyCode = GenerateNumberUtil.generateCodeNumber();
        User user = authMapper.mapFromRegisterCreateRequest(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setUuid(UUID.randomUUID().toString());
        user.setIsDelete(false);
        user.setIsVerified(false);
        user.setIsBlock(false);
        user.setVerificationCode(verifyCode);

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User role does not exist!"
                ));
        user.setRoles(new ArrayList<>(List.of(role)));

        userRepository.save(user);

        List<Invitation> pendingInvites = invitationRepository.findAllByEmail(user.getEmail().toLowerCase());

        if (!pendingInvites.isEmpty()) {
            pendingInvites.forEach(invite -> {
                WorkspaceMember member = new WorkspaceMember();
                member.setUser(user);
                member.setWorkspace(invite.getWorkspace());
                member.setRole(invite.getRole());
                workspaceMemberRepository.save(member);
            });

            invitationRepository.deleteAll(pendingInvites);
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        Context context = new Context();
        context.setVariable("verifyCode", user.getVerificationCode());

        String htmlTemplate = templateEngine.process("auth/verify-mail", context);
        helper.setTo(registerRequest.email());
        helper.setFrom(adminMail);
        helper.setSubject("IFinder verify code");
        helper.setText(htmlTemplate, true);

        javaMailSender.send(message);

        return BaseMessage.builder().message("Verify code has been send to your email. Please check your email.").build();
    }


    @Override
    @Transactional
    public BaseMessage verifyUserAccount(VerifyCodeRequest verifyCodeRequest) {
        User user = authRepository.findByEmailAndVerificationCodeAndIsDeleteFalse(verifyCodeRequest.email(), verifyCodeRequest.verificationCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User has not been found!"
                ));

        if (user.getIsVerified().equals(true)) {
            return BaseMessage.builder().message("Your account has already been verified.").build();
        }

        // ១. Update User status
        user.setIsVerified(true);
        userRepository.save(user);

        // ២.  Default Workspace ឱ្យ User (UX: My Workspace)
        Workspace defaultWorkspace = new Workspace();
        defaultWorkspace.setName(user.getName() + "'s Workspace");

        //  Project Key
        String uniqueKey = "p-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        defaultWorkspace.setProjectKey(uniqueKey);



        Workspace savedWorkspace = workspaceRepository.save(defaultWorkspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setUser(user);
        member.setWorkspace(savedWorkspace);
        member.setRole("OWNER");
        workspaceMemberRepository.save(member);


        createDefaultAuthService(savedWorkspace, user);

        Folder generalFolder = new Folder();
        generalFolder.setName("General");
        generalFolder.setWorkspace(savedWorkspace);
        folderRepository.save(generalFolder);

        return BaseMessage.builder().message("Your account has been verified successfully.").build();
    }
    private void createDefaultAuthService(Workspace workspace, User user) {
        String pKey = workspace.getProjectKey();

        String authSwaggerJson = """
    {
  "openapi": "3.0.0",
  "info": {
    "title": "Auth Service API",
    "description": "API សម្រាប់គ្រប់គ្រងការចុះឈ្មោះ និងចូលប្រើប្រាស់ (Dynamic JWT)",
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "https://api.idata.fit",
      "description": "Production Server"
    }
  ],
  "paths": {
    "/api/v1/engine-%s/auth/register": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Register new user",
        "responses": { "201": { "description": "User created" } }
      }
    },
    "/api/v1/engine-%s/auth/login": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Login to get tokens",
        "responses": { "200": { "description": "Login success" } }
      }
    },
    "/api/v1/engine-%s/auth/refresh": {
      "post": {
        "tags": ["Authentication"],
        "summary": "Refresh expired Access Token",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "refreshToken": { "type": "string" }
                },
                "example": { "refreshToken": "eyJhbG..." }
              }
            }
          }
        },
        "responses": { "200": { "description": "Token refreshed" } }
      }
    }
  }
}
""".formatted(pKey, pKey, pKey);

        Folder folder = new Folder();
        folder.setName("Auth Service");
        folder.setWorkspace(workspace);
        Folder savedFolder = folderRepository.save(folder);

        ApiScheme scheme = new ApiScheme();
        scheme.setName("auth");
        scheme.setType("AUTH");
        scheme.setWorkspace(workspace);
        scheme.setFolder(savedFolder);
        scheme.setOwner(user);
        scheme.setApiKey("sk_live_" + UUID.randomUUID().toString().replace("-", ""));

        scheme.setEndpointUrl("/api/v1/engine-" + pKey + "/auth");

        scheme.setDefinition(authSwaggerJson);

        scheme.setCreatedAt(LocalDateTime.now());
        scheme.setUpdatedAt(LocalDateTime.now());
        scheme.setIsPublic(false);
        scheme.setIsPublished(false);
        scheme.setForkCount(0);
        scheme.setViewCount(0);


        apiSchemeRepository.save(scheme);
    }



    @Override
    public AuthResponse userLogin(LoginRequest loginRequest) {
        if (!userRepository.existsByEmail(loginRequest.email())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User has not been found!"
            );
        }

        User user = userRepository.findByEmailAndIsBlockFalseAndIsDeleteFalse(loginRequest.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User has not been found!"
                ));

        if (!user.getIsVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account has not been verified."
            );
        }

        if(user.getRoles().stream().noneMatch(role -> role.getName().equals("USER"))){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You are not authorized to access this resource."
            );
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password()
        );

        Authentication authentication = daoAuthenticationProvider.authenticate(auth);
        // DEBUG: Print to console to see if tokens exist before returning
        AuthResponse response = authTokenService.createToken(authentication);
        log.info("Access Token: {}", response);

        return response;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return authTokenService.refreshToken(refreshTokenRequest);
    }

    @Override
    public BaseMessage sendVerifyCode(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User has not been found!"
                ));

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        String verifyCode = GenerateNumberUtil.generateCodeNumber();

        Token token = new Token();
        token.setUser(user);
        token.setVerifyCode(verifyCode);
        tokenRepository.save(token);

        Context context = new Context();
        context.setVariable("verifyCode", verifyCode);
        String htmlTemplate = templateEngine.process("auth/verify-mail", context);
        helper.setTo(email);
        helper.setFrom(adminMail);
        helper.setSubject("IFinder verify code");
        helper.setText(htmlTemplate, true);

        javaMailSender.send(message);
        return BaseMessage.builder()
                .message("Verify code has been send to your email. Please check your email.")
                .build();
    }

    @Override
    public ResetTokenResponse getResetToken(ResetTokenRequest resetTokenRequest) {
        User user = userRepository.findByEmail(resetTokenRequest.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User has not been found!"
                ));

        Token token = tokenRepository.findByUser(user);
        if (!token.getVerifyCode().equals(resetTokenRequest.verifyCode())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid verify code"
            );
        }

        return ResetTokenResponse.builder()
                .resetToken(authTokenService.generateResetToken(user.getEmail(), user, token))
                .email(user.getEmail())
                .verifyCode(token.getVerifyCode())
                .message("Reset token has been generated")
                .build();
    }

    @Override
    public BaseMessage resetPassword(ResetPasswordRequest resetPasswordRequest) {
        if (!resetPasswordRequest.newPassword().equals(resetPasswordRequest.confirmPassword())) {
            return BaseMessage.builder().message("New password and Confirm password must be same").build();
        }

        return authTokenService.resetPassword(resetPasswordRequest.resetToken(), resetPasswordRequest.newPassword());

    }
}
