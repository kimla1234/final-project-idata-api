package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "api_schemes")
public class ApiScheme extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String endpointUrl; // Example: "products" or "users-list"

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Stores the API structure in JSON format (Field name & Data type)
     * Example: {"name": "string", "price": "number"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> schemaStructure;

    @Column(nullable = false)
    private Boolean isPublic = false; // Flag for Community Showcase (Behance Style)

    @Column(name = "is_published")
    private Boolean isPublished = false; // Controls visibility in the Community section

    /**
     * Detailed field metadata: fieldName, type, and required status
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> properties;

    /**
     * Database-level constraints: columnName, primaryKey, foreignKey, and referenceTable
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> keys;

    /**
     * Self-referencing relationship for the Fork feature.
     * Stores the original API ID if this scheme was created via a Fork.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ApiScheme parentApi;

    @Column(columnDefinition = "int default 0")
    private Integer forkCount = 0;

    @Column(columnDefinition = "int default 0")
    private Integer viewCount = 0;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToOne(mappedBy = "apiScheme", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ProjectAnalytics analytics;

    // --- Auditing & Timestamps ---

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;

    @UpdateTimestamp // Automatically updates timestamp on every save operation
    private LocalDateTime updatedAt;

    @CreationTimestamp // Automatically sets timestamp on record creation
    private LocalDateTime createdAt;

    // --- Data & Configuration ---

    @OneToMany(mappedBy = "apiScheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiData> apiData;

    @Column(name = "api_key")
    private String apiKey; // Unique secret key for API access authentication

    @Column(columnDefinition = "TEXT")
    private String defaultResponse;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(name = "type", length = 20)
    private String type; // Possible values: "AUTH" or "DATA"
}