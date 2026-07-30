package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
@Table(name = "token_daily_summary", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usage_date", "model_id"})
}, indexes = {
        @Index(name = "idx_summary_date", columnList = "usage_date")
})
public class TokenDailySummary {
    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private Long id = Snowflake.nextId();

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Models model;

    @Column(name = "total_tokens", nullable = false)
    private Long totalTokens;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}