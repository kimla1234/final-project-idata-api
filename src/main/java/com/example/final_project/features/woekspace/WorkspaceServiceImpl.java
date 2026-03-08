package com.example.final_project.features.woekspace;

import com.example.final_project.domain.*;
import com.example.final_project.features.campaign.CampaignRepository;
import com.example.final_project.features.folder.FolderRepository;
import com.example.final_project.features.user.UserRepository;
import com.example.final_project.features.woekspace.dto.*;
import com.example.final_project.mapper.WorkspaceMapper;
import com.nimbusds.jwt.JWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final PasswordEncoder passwordEncoder;
    private final FolderRepository folderRepository;

    @Override
    @Transactional // បន្ថែម Transactional ដើម្បីធានាថាបើបង្កើត Folder បរាជ័យ Workspace ក៏មិនត្រូវបង្កើតដែរ
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest workspaceCreateRequest, Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ១. បង្កើត និងរក្សាទុក Workspace
        Workspace workspace = workspaceMapper.fromCreateRequest(workspaceCreateRequest);
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // ២. បង្កើត Record សមាជិកភាព និងកំណត់សិទ្ធិជា OWNER
        WorkspaceMember member = new WorkspaceMember();
        member.setUser(user);
        member.setWorkspace(savedWorkspace);
        member.setRole("OWNER");
        workspaceMemberRepository.save(member);

        // ៣. បង្កើត Default Folder ស្វ័យប្រវត្តិ
        Folder defaultFolder = new Folder();
        defaultFolder.setName("General"); // ឬដាក់ឈ្មោះអ្វីដែលអ្នកចង់បាន
        defaultFolder.setWorkspace(savedWorkspace);
        // ប្រសិនបើ Folder Entity របស់អ្នកមាន field ផ្សេងៗដូចជា createdAt សូមកំណត់នៅទីនេះ
        folderRepository.save(defaultFolder);

        return workspaceMapper.toResponse(savedWorkspace);
    }
    @Override
    public List<WorkspaceResponse> getMyWorkspaces(Jwt jwt) {
        // ត្រូវប្រើ "sub" ឱ្យដូចគ្នាទៅនឹងពេល Create (iss គឺជា Identity Provider មិនមែន Email ទេ)
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

        // ឆែកមើលថា តើ User ហ្នឹងជាសមាជិកនៃ Workspace ហ្នឹងមែនអត់ មុននឹងឱ្យទិន្នន័យទៅ
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលមើល Workspace នេះទេ"));

        return workspaceMapper.toResponse(membership.getWorkspace());
    }

    @Override
    @Transactional
    public void inviteMember(Integer workspaceId, WorkspaceInviteRequest request, Jwt jwt) {
        String inviterEmail = jwt.getClaimAsString("sub");


        // ១. ឆែកសិទ្ធិអ្នក Invite (ត្រូវតែជា OWNER ឬ ADMIN)
        WorkspaceMember inviter = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, inviterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមែនជាសមាជិកនៃ Workspace នេះទេ"));

        if (!List.of("OWNER", "ADMIN").contains(inviter.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិផ្ញើការអញ្ជើញឡើយ");
        }

        // ២. រកមើល User ដែលត្រូវ Invite តាម Email
        User invitee = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ User ដែលមាន Email នេះឡើយ"));

        // ៣. ឆែកមើលក្រែងលោគាត់ជាសមាជិករួចហើយ
        boolean isAlreadyMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.getId());
        if (isAlreadyMember) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User នេះជាសមាជិករួចរាល់ហើយ");
        }

        // ៤. បង្កើតសមាជិកភាពថ្មី
        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setUser(invitee);
        newMember.setWorkspace(inviter.getWorkspace());

        try {
            // បំប្លែង String ពី Request (ADMIN, EDITOR, VIEWER) ទៅជា Enum WorkspaceRole
            WorkspaceRole roleEnum = WorkspaceRole.valueOf(request.role().toUpperCase());
            newMember.setRole(String.valueOf(roleEnum));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ប្រភេទ Role មិនត្រឹមត្រូវ៖ " + request.role());
        }

        workspaceMemberRepository.save(newMember);
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

        // ១. ឆែកមើលថាអ្នកសួរ ជាសមាជិកនៃ Workspace ហ្នឹងមែនអត់
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិមើលសមាជិកក្នុង Workspace នេះទេ");
        }

        // ២. ទាញយកសមាជិកទាំងអស់ក្នុង Workspace នោះ
        List<WorkspaceMember> members = workspaceMemberRepository.findAllByWorkspaceId(workspaceId);

        // ៣. បំប្លែងពី WorkspaceMember Entity ទៅជា MemberResponse DTO
        return members.stream()
                .map(m -> new MemberResponse(
                        m.getId(),
                        m.getUser().getName(),
                        m.getUser().getEmail(),
                        m.getRole(),
                        m.getUser().getProfileImage() // កែសម្រួលតាម Entity របស់អ្នក
                ))
                .toList();
    }

    @Override
    @Transactional
    public void deleteWorkspace(Integer id, DeleteWorkspaceRequest request, Jwt jwt) {
        // ១. ទាញយក Email និងស្វែងរក User (ប្រើ sub ដូច Method ផ្សេងៗ)
        String currentUserEmail = jwt.getClaimAsString("sub");
        if (currentUserEmail == null) currentUserEmail = jwt.getSubject();

        final String finalEmail = currentUserEmail;

        // ២. ស្វែងរក Workspace
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Workspace ឡើយ"));

        // ៣. ស្វែងរក User ដើម្បីឆែក Password
        User user = userRepository.findByEmail(finalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញអ្នកប្រើប្រាស់ម្នាក់នេះឡើយ"));

        // ៤. ផ្ទៀងផ្ទាត់ Password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password មិនត្រឹមត្រូវឡើយ!");
        }

        // ៥. ឆែកមើលថាគាត់ជា OWNER មែនឬអត់ (តាមរយៈ WorkspaceMember Table)
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(id, finalEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិក្នុង Workspace នេះទេ"));

        if (!"OWNER".equalsIgnoreCase(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "មានតែម្ចាស់ (OWNER) ប៉ុណ្ណោះដែលអាចលុបបាន");
        }

        // --- ដំណោះស្រាយចំពោះ Error TransientPropertyValueException ---

        // ៦. លុបសមាជិកទាំងអស់ចេញពី Workspace នេះជាមុនសិន (ដើម្បីកុំឱ្យជាប់ Foreign Key)
        workspaceMemberRepository.deleteAllByWorkspaceId(id);

        // ៧. លុប Workspace ជាស្ថាពរ
        workspaceRepository.delete(workspace);
    }


    @Override
    public WorkspaceResponse updateWorkspace(Integer id, WorkspaceUpdateRequest request, Jwt jwt) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        // ... ឆែក Permission (isMember) ...

        workspace.setName(request.name());

        // ឆែកមើល description សិន ការពារកុំឱ្យវា Overwrite ទិន្នន័យចាស់ជាមួយ null
        if (request.description() != null) {
            workspace.setDescription(request.description());
        }

        return workspaceMapper.toResponse(workspaceRepository.save(workspace));
    }

    @Override
    public WorkspaceStatsResponse getAllCampaigns(Integer workspaceId, Jwt jwt) {
        checkWorkspaceAccess(workspaceId, jwt);

        List<Campaign> campaigns = campaignRepository.findAllByWorkspaceId(workspaceId);

        long totalCampaigns = campaigns.size();
        long totalSuccess = campaigns.stream().mapToLong(Campaign::getSuccessCount).sum();
        long totalFailure = campaigns.stream().mapToLong(Campaign::getFailureCount).sum();
        long totalEmails = totalSuccess + totalFailure;

        double successRate = totalEmails > 0 ? (double) totalSuccess / totalEmails * 100 : 0;

        return new WorkspaceStatsResponse(
                totalCampaigns,
                totalEmails,
                totalSuccess,
                totalFailure,
                successRate
        );
    }

    private void checkWorkspaceAccess(Integer workspaceId, Jwt jwt) {
        String email = jwt.getClaimAsString("sub"); // ទាញយក Email ពី Token
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserEmail(workspaceId, email);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកគ្មានសិទ្ធិចូលប្រើប្រាស់ Workspace នេះទេ");
        }
    }
}
