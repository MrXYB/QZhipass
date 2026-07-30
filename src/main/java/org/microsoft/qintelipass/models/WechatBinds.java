package org.microsoft.qintelipass.models;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
public class WechatBinds {
    @Id
    @Column(updatable = false, nullable = false, unique = true)
    private Long bindId;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(length = 50)
    private String wechat;
}
