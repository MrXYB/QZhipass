package org.microsoft.qintelipass.entity.hotkey;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    private int index;
    @NotBlank
    @Column(nullable = false)
    private String functionKey;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt;

}
