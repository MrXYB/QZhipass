package org.microsoft.qintelipass.entity.hotkey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.IdGeneratorType;

@Setter
@Getter
@Entity
@Table(name = "functions")
public class Function {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int funcId;
    private String funcName;
}
