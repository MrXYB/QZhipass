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
@Table(name = "censor_alert_rules")
public class CensorAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "period_days", nullable = false)
    private int periodDays = 1;

    @Column(name = "threshold", nullable = false)
    private int threshold = 3;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "is_default", nullable = false)
    private boolean defaultRule = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
