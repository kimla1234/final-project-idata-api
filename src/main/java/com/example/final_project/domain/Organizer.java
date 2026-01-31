package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "organizers")
public class Organizer extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private String phoneNumber;
    private String logoImage;
    private String description;
    private String address;
    private Boolean status = true;
    private Boolean isBlock = false;
    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User createdBy;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events;


}
