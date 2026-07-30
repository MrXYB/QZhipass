package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.enums.CensorAlertStatus;
import org.microsoft.qintelipass.entity.CensorAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CensorAlertRepository extends JpaRepository<CensorAlert, Long>, JpaSpecificationExecutor<CensorAlert> {
    Page<CensorAlert> findAllByOrderByTriggeredAtDesc(Pageable pageable);

    Page<CensorAlert> findByNameContainingIgnoreCaseOrEmployeeIdContainingIgnoreCaseOrderByTriggeredAtDesc(
            String name,
            String employeeId,
            Pageable pageable
    );

    long countByStatus(CensorAlertStatus status);

    long countByTriggeredAtBetween(LocalDateTime start, LocalDateTime end);

    Optional<CensorAlert> findFirstByUser_IdAndStatusOrderByTriggeredAtDesc(Long userId, CensorAlertStatus status);

    Optional<CensorAlert> findFirstByUser_IdAndTriggeredAtBetweenOrderByTriggeredAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
