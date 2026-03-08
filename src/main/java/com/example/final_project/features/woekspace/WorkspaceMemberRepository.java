package com.example.final_project.features.woekspace;

import com.example.final_project.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findAllByUserEmail(String email);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserEmail(Integer workspaceId, String email);

    boolean existsByWorkspaceIdAndUserId(Integer workspaceId, Integer id);

    boolean existsByWorkspaceIdAndUserEmail(Integer workspaceId, String email);

    List<WorkspaceMember> findAllByWorkspaceId(Integer workspaceId);
    void deleteAllByWorkspaceId(Integer workspaceId);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserUuid(Integer workspaceId, String uuid);
}
