package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project_analytics")
public class ProjectAnalytics extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_scheme_id", nullable = false)
    private ApiScheme apiScheme;

    @Column(columnDefinition = "int default 0")
    private Integer totalRequests = 0;

    @Column(columnDefinition = "int default 0")
    private Integer totalForks = 0;

    @Column(columnDefinition = "int default 0")
    private Integer totalViews = 0;

    private LocalDateTime lastAccessedAt;


    @Column(length = 100)
    private String topReferrer;
}