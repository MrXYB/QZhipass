package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.hotkey.Hotkey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotkeyRepository extends JpaRepository<Hotkey, Integer> {
}
