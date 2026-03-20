package com.example.final_project.domain;

import com.example.final_project.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="mock_data")
public class MockData extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * ផ្ទុកទិន្នន័យជាក់ស្តែង (ឧទាហរណ៍៖ {"id": 1, "name": "Coca Cola", "price": 0.5})
     * យើងប្រើ Map<String, Object> ដើម្បីឱ្យវាអាចបត់បែនតាមគ្រប់ Schema ទាំងអស់
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> dataJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_scheme_id", nullable = false)
    private ApiScheme apiScheme;
}