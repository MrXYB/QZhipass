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
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int keyId;
    private String keyName;
}
