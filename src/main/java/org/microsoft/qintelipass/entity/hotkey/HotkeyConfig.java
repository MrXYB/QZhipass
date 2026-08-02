package org.microsoft.qintelipass.entity.hotkey;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.microsoft.qintelipass.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(HotkeyConfigID.class)
public class HotkeyConfig {
    @Id
    private Long userId;
    @Id
    private int funcId;
    @Column(nullable = false)
    private int keyId;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt;

}
