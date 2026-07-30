package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.microsoft.qintelipass.util.Snowflake;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token_usage_logs", indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, usage_date"),
        @Index(name = "idx_model_date", columnList = "model_id, usage_date")
})
public class TokenUsageLog {
    @Id
    @Column(name = "log_id", updatable = false, nullable = false, unique = true)
    private Long id = Snowflake.nextId();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Models model;

    @Column(name = "tokens_used", nullable = false)
    private Integer tokensUsed;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}