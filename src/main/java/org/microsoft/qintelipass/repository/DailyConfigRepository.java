package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.DailyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyConfigRepository extends JpaRepository<DailyConfig, Long> {
    Optional<DailyConfig> findByUser_Id(Long userId);
    Optional<DailyConfig> findByUser_IdAndModelId(Long userId, Long modelId);
}