package com.example.final_project.features.woekspace;

import com.example.final_project.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findAllByEmail(String email);

    Invitation findByToken(String token);

    Optional<Invitation> findByWorkspaceIdAndEmail(Integer workspaceId, String targetEmail);

    List<Invitation> findByWorkspaceId(Integer workspaceId);

    void deleteByWorkspaceIdAndEmail(Integer workspaceId, String email);
}