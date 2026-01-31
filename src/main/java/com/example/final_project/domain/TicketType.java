package com.example.final_project.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_types")
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String name;        // e.g., "VIP", "PREMIUM", "REGULAR"
    private Double price;       // តម្លៃ (បង្ហាញក្នុង Column REVENUE)
    private int total_quantity; // ចំនួនសរុប (លេខខាងក្រោយក្នុង Column SOLD ឧទាហរណ៍: 12)
    private int sold_quantity;  // ចំនួនលក់ដាច់ (លេខខាងមុខក្នុង Column SOLD ឧទាហរណ៍: 10)

    private Boolean is_published; // សម្រាប់ Column PUBLISH (Published/Unpublished)
    private Boolean is_display;   // សម្រាប់ Column DISPLAY (Show/Hide icon)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Event event;
}
