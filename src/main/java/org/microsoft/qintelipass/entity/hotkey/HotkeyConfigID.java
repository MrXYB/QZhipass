package org.microsoft.qintelipass.entity.hotkey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotkeyConfigID implements Serializable {
    private Long userId;
    private int index;
}
