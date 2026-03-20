package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(length = 80)
    private String name;

    @Column(length = 10)
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 30)
    private String email;

    @Column(length = 80)
    private String password;

    @Column(length = 30)
    private String phone ;

    private String profileImage;

    private String coverImage;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;


    private String verificationCode;

    private Boolean status = true;     // Default to true so the account is "active"
    private Boolean isBlock = false;    // Default to false
    private Boolean isVerified = false; // Default to false until they enter OTP



    @ManyToMany
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles;

    @ManyToMany
    @JoinTable(name = "users_workspaces",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "workspace_id"))
    private List<Workspace> workspaces;


    @ManyToMany
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"),      // ID របស់អ្នកដែលត្រូវគេ Follow
            inverseJoinColumns = @JoinColumn(name = "follower_id") // ID របស់អ្នកដែលទៅចុច Follow គេ
    )
    private List<User> followers;

    // បញ្ជីអ្នកដែលយើងទៅ Follow គេ (ភ្ញៀវ)
    @ManyToMany(mappedBy = "followers")
    private List<User> following;

    public void setIsDelete(boolean b) {
    }
}
