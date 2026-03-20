package com.example.final_project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Getter
@Setter
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String role;
    private String token; // UUID សម្រាប់ប្រើក្នុង Link
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
}