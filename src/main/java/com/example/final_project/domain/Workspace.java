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

    // Workspace មួយមានមនុស្សចូលរួមច្រើន (Owner, Staff, etc.)
    @ManyToMany(mappedBy = "workspaces")
    private List<User> users;

    // Workspace មួយអាចមាន Folder ច្រើន
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Folder> folders;

    // Workspace មួយអាចមាន Tenant ច្រើន
    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Tenant> tenants;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role; // OWNER, ADMIN, etc.


}
