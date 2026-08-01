package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "wechat_binds")
public class WechatBinds {
    @Id
    @Column(updatable = false, nullable = false, unique = true)
    private Long bindId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String wechat;

    @CreationTimestamp
    @Column(name = "bind_at")
    private LocalDateTime bindAt;
}
