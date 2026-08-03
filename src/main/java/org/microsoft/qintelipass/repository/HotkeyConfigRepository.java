package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.hotkey.HotkeyConfig;
import org.microsoft.qintelipass.entity.hotkey.HotkeyConfigID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotkeyConfigRepository extends JpaRepository<HotkeyConfig, HotkeyConfigID> {
    List<HotkeyConfig> findAllByUserIdIs(Long userId);
}
