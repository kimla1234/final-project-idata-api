package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workspace_members")
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceMember extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    private String role; // "OWNER", "ADMIN", "MEMBER"

    public WorkspaceMember(User user, Workspace workspace, String role) {
        this.user = user;
        this.workspace = workspace;
        this.role = role;
    }
}