package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

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

    @CreatedDate
    @CreationTimestamp
    private Date bindAt;
}
