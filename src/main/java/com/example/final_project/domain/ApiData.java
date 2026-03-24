package com.example.final_project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "api_data")
@Getter
@Setter

public class ApiData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JoinColumn(name = "api_scheme_id")
    private ApiScheme apiScheme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> content;

    private LocalDateTime createdAt = LocalDateTime.now();
    @JdbcTypeCode(SqlTypes.JSON) //  Dynamic (email, password, etc.)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> jsonData;
}