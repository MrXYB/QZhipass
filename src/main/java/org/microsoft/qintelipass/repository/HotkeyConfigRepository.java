package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.hotkey.HotkeyConfig;
import org.microsoft.qintelipass.entity.hotkey.HotkeyConfigID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotkeyConfigRepository extends JpaRepository<HotkeyConfig, HotkeyConfigID> {
}
