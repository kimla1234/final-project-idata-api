package com.example.final_project.features.woekspace;

import com.example.final_project.domain.*;
import com.example.final_project.features.apiScheme.ApiSchemeRepository;
import com.example.final_project.features.folder.FolderRepository;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.features.woekspace.dto.*;
import com.example.final_project.mapper.WorkspaceMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context; // 🎯 ប្រយ័ត្ន Import ខុស Context របស់ Class ផ្សេង
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FolderRepository folderRepository;
    private final InvitationRepository invitationRepository;
    private final ApiSchemeRepository apiSchemeRepository;

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String adminMail;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest workspaceCreateRequest, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Workspace workspace = workspaceMapper.fromCreateRequest(workspaceCreateRequest);
        String randomKey = "p-" + UUID.randomUUID().toString().substring(0, 8);
        workspace.setProjectKey(randomKey);
        Workspace savedWorkspace = workspaceRepository.save(workspace);

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

        return workspaceMapper.toResponse(savedWorkspace);
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
    """.formatted(pKey, pKey,pKey);

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
    public List<WorkspaceResponse> getMyWorkspaces(Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        List<WorkspaceMember> memberships = workspaceMemberRepository.findAllByUserEmail(email);

        List<Workspace> workspaces = memberships.stream()
                .map(WorkspaceMember::getWorkspace)
                .toList();

        return workspaceMapper.toResponseList(workspaces);
    }

    @Override
    public WorkspaceResponse getWorkspaceById(Integer id, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");

        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើល Workspace នេះទេ"));

        return workspaceMapper.toResponse(membership.getWorkspace());
    }

    @Override
    @Transactional
    public void inviteMember(Integer workspaceId, WorkspaceInviteRequest request, Jwt jwt) {
        String inviterEmail = jwt.getClaimAsString("sub");
        String targetEmail = request.email().trim().toLowerCase();

        WorkspaceMember inviter = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, inviterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"));

        Workspace workspace = inviter.getWorkspace();

        boolean isAlreadyMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, targetEmail);
        if (isAlreadyMember) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a member of this workspace.");
        }

        Optional<Invitation> existingInvite = invitationRepository.findByWorkspaceIdAndEmail(workspaceId, targetEmail);
        if (existingInvite.isPresent() && existingInvite.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An active invitation has already been sent to this email.");
        }

        Invitation invitation = existingInvite.orElse(new Invitation());
        invitation.setEmail(targetEmail);
        invitation.setRole(request.role().toUpperCase());
        invitation.setWorkspace(workspace);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiryDate(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invitation);

        try {
            sendInvitationEmail(targetEmail, workspace.getName(), workspaceId);
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
        }
    }



    private void saveWorkspaceMember(User user, Workspace workspace, String role) {
        WorkspaceMember member = new WorkspaceMember();
        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(role.toUpperCase());
        workspaceMemberRepository.save(member);
    }

    @Override
    public void updateMemberRole(Integer workspaceId, Integer memberId, WorkspaceRoleUpdateRequest request, Jwt jwt) {
        // 1. ប្រើ getClaimAsString("sub") ដើម្បីទាញយក Email
        String currentUserEmail = jwt.getClaimAsString("sub");

        // 2. ប្រើ Method ណាដែលស្វែងរកតាម Email (ដូចក្នុង getWorkspaceMembers)
        WorkspaceMember requester = workspaceMemberRepository
                .findByWorkspaceIdAndUserEmail(workspaceId, currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិក្នុង Workspace នេះទេ"));

        if (!requester.getRole().equals("OWNER") && !requester.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "មានតែ Owner ឬ Admin ទេដែលអាចប្តូរ Role បាន");
        }

        // 3. ត្រូវប្រើ Long.valueOf ឬកែសម្រួល Repository ឱ្យទទួល Integer
        WorkspaceMember memberToUpdate = workspaceMemberRepository
                .findById(Long.valueOf(memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញសមាជិកនេះឡើយ"));

        if (memberToUpdate.getRole().equals("OWNER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "មិនអាចប្តូរ Role របស់ម្ចាស់ Workspace បានទេ");
        }

        memberToUpdate.setRole(request.role().toUpperCase());
        workspaceMemberRepository.save(memberToUpdate);
    }
    @Override
    public List<MemberResponse> getWorkspaceMembers(Integer workspaceId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        checkWorkspaceAccess(workspaceId, jwt);

        List<WorkspaceMember> members = workspaceMemberRepository.findAllByWorkspaceId(workspaceId);
        List<MemberResponse> responseList = new java.util.ArrayList<>(members.stream()
                .map(m -> new MemberResponse(
                        m.getId().intValue(),
                        m.getUser().getName(),
                        m.getUser().getEmail(),
                        m.getRole(),
                        m.getUser().getProfileImage()
                )).toList());

        List<Invitation> pendingInvites = invitationRepository.findByWorkspaceId(workspaceId);
        pendingInvites.forEach(invite -> {
            responseList.add(new MemberResponse(
                    null,
                    "Pending User",
                    invite.getEmail(),
                    invite.getRole() + " (PENDING)",
                    "default-avatar.png"
            ));
        });

        return responseList;
    }

    @Override
    @Transactional
    public void deleteWorkspace(Integer id, DeleteWorkspaceRequest request, Jwt jwt) {
        String currentUserEmail = jwt.getClaimAsString("sub");
        if (currentUserEmail == null) currentUserEmail = jwt.getSubject();

        final String finalEmail = currentUserEmail;

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Workspace ឡើយ"));

        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញអ្នកប្រើប្រាស់ម្នាក់នេះឡើយ"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password មិនត្រឹមត្រូវឡើយ!");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(id, finalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិក្នុង Workspace នេះទេ"));

        if (!"OWNER".equalsIgnoreCase(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "មានតែម្ចាស់ (OWNER) ប៉ុណ្ណោះដែលអាចលុបបាន");
        }


        workspaceMemberRepository.deleteAllByWorkspaceId(id);

        workspaceRepository.delete(workspace);
    }


    @Override
    public WorkspaceResponse updateWorkspace(Integer id, WorkspaceUpdateRequest request, Jwt jwt) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));



        workspace.setName(request.name());

        if (request.description() != null) {
            workspace.setDescription(request.description());
        }

        return workspaceMapper.toResponse(workspaceRepository.save(workspace));
    }

    @Override
    public WorkspaceStatsResponse getAllCampaigns(Integer workspaceId, Jwt jwt) {
        return null;
    }

    private void checkWorkspaceAccess(Integer workspaceId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលប្រើប្រាស់ Workspace នេះទេ");
        }
    }


    @Transactional
    @Override
    public void joinWorkspace(Integer workspaceId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub").toLowerCase();

        // ១. ស្វែងរកការអញ្ជើញក្នុង Table invitations
        List<Invitation> invitations = invitationRepository.findAllByEmail(email);

        // ចាប់យក Invitation ណាដែលត្រូវនឹង Workspace ID នេះ
        Invitation currentInvite = invitations.stream()
                .filter(i -> i.getWorkspace().getId().equals(workspaceId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការអញ្ជើញសម្រាប់ Workspace នេះទេ"));

        // ២. ឆែកមើលសុពលភាព (Expiry Date)
        if (currentInvite.getExpiryDate().isBefore(LocalDateTime.now())) {
            invitationRepository.delete(currentInvite);
            throw new ResponseStatusException(HttpStatus.GONE, "ការអញ្ជើញនេះបានហួសសុពលភាពហើយ");
        }

        // ៣. បង្កើតសមាជិកភាពពិតប្រាកដ
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ User ឡើយ"));

        saveWorkspaceMember(user, currentInvite.getWorkspace(), currentInvite.getRole());

        // ៤. លុប Pending Invitation ចោល (ព្រោះក្លាយជាសមាជិកពេញសិទ្ធិហើយ)
        invitationRepository.delete(currentInvite);
    }
    private void sendInvitationEmail(String targetEmail, String workspaceName, Integer workspaceId) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("workspaceName", workspaceName);
        String joinLink = "https://ui.idata.fit/join/" + workspaceId;
        thymeleafContext.setVariable("joinLink", joinLink);

        String htmlTemplate = templateEngine.process("auth/workspace/invite-mail", thymeleafContext);

        helper.setTo(targetEmail);
        helper.setFrom(adminMail);
        helper.setSubject("You are invited to join " + workspaceName + " workspace");
        helper.setText(htmlTemplate, true);

        javaMailSender.send(message);
    }


    @Override
    @Transactional
    public void removeMember(Integer workspaceId, Integer memberId, Jwt jwt) {
        String currentUserEmail = jwt.getClaimAsString("sub");

        WorkspaceMember requester = workspaceMemberRepository
                .findByWorkspaceIdAndUserEmail(workspaceId, currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"));

        if (!requester.getRole().equals("OWNER") && !requester.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "មានតែ Owner ឬ Admin ទេដែលអាចលុបសមាជិកបាន");
        }

        WorkspaceMember memberToDelete = workspaceMemberRepository
                .findById(Long.valueOf(memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញសមាជិកនេះឡើយ"));

        if (memberToDelete.getRole().equals("OWNER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "មិនអាចលុបម្ចាស់ Workspace បានទេ");
        }

        workspaceMemberRepository.delete(memberToDelete);
    }

    @Override
    @Transactional
    public void revokeInvitation(Integer workspaceId, String email, Jwt jwt) {
        String currentUserEmail = jwt.getClaimAsString("sub");

        WorkspaceMember requester = workspaceMemberRepository
                .findByWorkspaceIdAndUserEmail(workspaceId, currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"));

        if (!requester.getRole().equals("OWNER") && !requester.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "មានតែ Owner ឬ Admin ទេដែលអាចលុបការអញ្ជើញបាន");
        }

        invitationRepository.deleteByWorkspaceIdAndEmail(workspaceId, email);
    }


}
