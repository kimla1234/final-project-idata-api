package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String uuid;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private Date start_date;
    private Date end_date;
    private String start_time;
    private String end_time;
    private String image;
    private String seat_plan;
    private String location_name;
    private String latitude;
    private String longitude;
    private Boolean status;
    private Boolean isDeleted = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private Set<String> imagesEvent = new HashSet<>();

    // Relationships Table Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private EventCategory category;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = true)
    private Organizer organizer;



    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketType> ticketTypes = new HashSet<>();

}
