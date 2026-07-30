package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.microsoft.qintelipass.util.Snowflake;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;

@Setter
@Getter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_config")
public class DailyConfig {
    @Id
    @Column(name = "config_id", updatable = false, nullable = false, unique = true)
    private Long id = Snowflake.nextId();
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "daily_limit", nullable = false)
    private Long dailyLimit;
    @OneToOne
    @JoinColumn(name = "model_id")
    private Models modelId;
    @CreatedDate
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}