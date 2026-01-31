package com.example.final_project.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_categories")
public class EventCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name; // Concert, Workshop, Sport

    private String icon; // សម្រាប់ទុក icon បង្ហាញលើ UI (ឧទាហរណ៍៖ "music", "school")

    private Boolean status = true;

    @OneToMany(mappedBy = "category")
    private List<Event> events;
}
