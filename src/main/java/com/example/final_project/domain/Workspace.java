package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="workspaces")
public class Workspace extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;

    @ManyToMany(mappedBy = "workspaces")
    private List<User> users;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Folder> folders;

    @Column(unique = true, nullable = false)
    private String projectKey;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role; // OWNER, ADMIN, etc.


}
