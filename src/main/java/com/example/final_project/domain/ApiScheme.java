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
    private String endpointUrl; // ឧទាហរណ៍: "products" ឬ "users-list"

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * ផ្ទុករចនាសម្ព័ន្ធ API ជាទម្រង់ JSON (Field name & Data type)
     * ឧទាហរណ៍: {"name": "string", "price": "number"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> schemaStructure;

    @Column(nullable = false)
    private Boolean isPublic = false; // សម្រាប់បង្ហាញក្នុង Community (Behance Style)

    @Column(name = "is_published")
    private Boolean isPublished = false; // សម្រាប់ Community Visibility

    // នៅក្នុង ApiScheme Domain
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> properties; // ទុក fieldName, type, required

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> keys; // ទុក columnName, primaryKey, foreignKey, referenceTable

    /**
     * ប្រើសម្រាប់មុខងារ Fork
     * ប្រសិនបើ API នេះបានមកពីការ Fork វានឹងរក្សាទុក ID របស់ API ដើម
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ApiScheme parentApi;

    // ប្តូរពី defaultValue = "0" មកជា columnDefinition វិញ
    @Column(columnDefinition = "int default 0")
    private Integer forkCount = 0;

    @Column(columnDefinition = "int default 0")
    private Integer viewCount = 0;

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

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;

    @UpdateTimestamp // វានឹង Update ម៉ោងអូតូឱ្យរាល់ពេលបងហៅ save()
    private LocalDateTime updatedAt;

    @CreationTimestamp // សម្រាប់ថ្ងៃបង្កើតដំបូង
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "apiScheme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiData> apiData;

    @Column(name = "api_key")
    private String apiKey;

    @Column(columnDefinition = "TEXT")
    private String defaultResponse;// លេខកូដសម្ងាត់សម្រាប់ API នីមួយៗ
    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(name = "type", length = 20)
    private String type; // តម្លៃអាចជា "AUTH" ឬ "DATA"
}