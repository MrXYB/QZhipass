package org.microsoft.qintelipass.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "censor_alerts",
        indexes = {
                @Index(name = "idx_censor_alerts_user_status", columnList = "user_id,status"),
                @Index(name = "idx_censor_alerts_triggered_at", columnList = "triggered_at"),
                @Index(name = "idx_censor_alerts_status", columnList = "status")
        }
)
public class CensorAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "employee_id", nullable = false, updatable = false, length = 50)
    private String employeeId;

    @Column(name = "name", nullable = false, updatable = false, length = 100)
    private String name;

    @Column(name = "department", updatable = false, length = 100)
    private String department;

    @Column(name = "position", updatable = false, length = 100)
    private String position;

    @Column(name = "rule_name", nullable = false, updatable = false, length = 100)
    private String ruleName;

    @Column(name = "period_days", nullable = false, updatable = false)
    private int periodDays;

    @Column(name = "threshold", nullable = false, updatable = false)
    private int threshold;

    @Column(name = "trigger_count", nullable = false, updatable = false)
    private long triggerCount;

    @Column(name = "current_count", nullable = false)
    private long currentCount;

    @Column(name = "keywords", nullable = false, updatable = false, length = 1000)
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CensorAlertStatus status = CensorAlertStatus.PENDING;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "notice_sent_at", updatable = false)
    private LocalDateTime noticeSentAt;

    @Column(name = "email", updatable = false, length = 150)
    private String email;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "handled_by", length = 100)
    private String handledBy;

    @Column(name = "contexts", length = 4000)
    private String contexts;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (triggeredAt == null) {
            triggeredAt = now;
        }
        if (noticeSentAt == null) {
            noticeSentAt = triggeredAt;
        }
    }
}
