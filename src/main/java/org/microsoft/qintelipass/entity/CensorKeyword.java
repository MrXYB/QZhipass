package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "censor_keywords",
        indexes = {
                @Index(name = "idx_censor_keywords_enabled", columnList = "enabled"),
                @Index(name = "idx_censor_keywords_risk_level", columnList = "risk_level"),
                @Index(name = "idx_censor_keywords_category", columnList = "category")
        }
)
public class CensorKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, length = 20)
    private String code;

    @Column(name = "keyword", nullable = false, unique = true, length = 100)
    private String keyword;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "risk_level", length = 10)
    private String riskLevel;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "trigger_count", nullable = false)
    private long triggerCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CensorKeyword() {
    }

    public CensorKeyword(String keyword) {
        this.keyword = keyword;
    }
}