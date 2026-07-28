package org.microsoft.qintelipass.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "censor_records",
        indexes = {
                @Index(name = "idx_censor_records_user_id", columnList = "user_id"),
                @Index(name = "idx_censor_records_created_at", columnList = "created_at"),
                @Index(name = "idx_censor_records_user_month", columnList = "user_id,created_at")
        }
)
public class CensorRecord {
    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "username", nullable = false, updatable = false, length = 100)
    private String username;

    @Column(name = "phone", nullable = false, updatable = false, length = 30)
    private String phone;

    @Column(name = "department", nullable = false, updatable = false, length = 100)
    private String department;

    @Column(name = "model_name", nullable = false, updatable = false, length = 100)
    private String modelName;

    @Column(name = "hit_keywords", nullable = false, updatable = false, length = 1000)
    private String hitKeywords;

    @Column(name = "input_excerpt", updatable = false, length = 2000)
    private String inputExcerpt;

    @Column(name = "output_excerpt", updatable = false, length = 2000)
    private String outputExcerpt;

    @Setter
    @Column(name = "admin_notified", nullable = false)
    private boolean adminNotified = false;

    @Setter
    @Column(name = "alert_counted", nullable = false)
    private boolean alertCounted = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CensorRecord() {
    }

    public CensorRecord(Long userId,
                        String username,
                        String phone,
                        String department,
                        String modelName,
                        String hitKeywords,
                        String inputExcerpt,
                        String outputExcerpt) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.department = department;
        this.modelName = modelName;
        this.hitKeywords = hitKeywords;
        this.inputExcerpt = inputExcerpt;
        this.outputExcerpt = outputExcerpt;
    }
    
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
