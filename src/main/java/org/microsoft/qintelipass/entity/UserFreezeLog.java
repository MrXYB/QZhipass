package org.microsoft.qintelipass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.microsoft.qintelipass.enums.UserFreezeAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_freeze_logs",
        indexes = {
                @Index(name = "idx_user_freeze_logs_user_time", columnList = "user_id,operated_at"),
                @Index(name = "idx_user_freeze_logs_alert", columnList = "censor_alert_id")
        }
)
public class UserFreezeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 100)
    private String operatorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private UserFreezeAction action;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "censor_alert_id")
    private Long censorAlertId;

    @Column(name = "previous_token_limit")
    private Long previousTokenLimit;

    @Column(name = "notification_message", length = 500)
    private String notificationMessage;

    @Column(name = "operated_at", nullable = false)
    private LocalDateTime operatedAt;

    @PrePersist
    void prePersist() {
        if (operatedAt == null) {
            operatedAt = LocalDateTime.now().withNano(0);
        }
    }
}