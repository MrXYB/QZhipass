package org.microsoft.qintelipass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.microsoft.qintelipass.enums.UserRole;
import org.microsoft.qintelipass.enums.UserStatus;
import org.microsoft.qintelipass.util.Snowflake;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @Column(name = "user_id", updatable = false, nullable = false, unique = true)
    private Long id = Snowflake.nextId();

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "varchar(20) default 'USER'")
    private UserRole role = UserRole.USER;

    @Column(name = "username", nullable = false, unique = true)
    private String name;

    @Column(name = "department")
    private String department;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean restored = false;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = UserStatus.NORMAL;
        }
        if (this.role == null) {
            this.role = UserRole.USER;
        }
        if (this.restored == null) {
            this.restored = false;
        }
    }

    @JsonIgnore
    public boolean isActive() {
        return this.status == UserStatus.NORMAL;
    }

    @JsonIgnore
    public boolean isCancelled() {
        return this.status == UserStatus.CANCELLED;
    }

    @JsonIgnore
    public boolean isFrozen() {
        return this.status == UserStatus.FROZEN;
    }
}
