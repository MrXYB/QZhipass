package org.microsoft.qintelipass.entity.hotkey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "hotkeys")
public class Hotkey {
    @Id
    @Column(name = "key_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer index;

    private String key;
}
