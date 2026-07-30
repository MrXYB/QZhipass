package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.CensorRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
public interface CensorRecordRepository extends JpaRepository<CensorRecord, Long> {

    long countByUser_IdAndCreatedAtBetween(Long userId,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime);

    long countByUser_IdAndAlertCountedTrueAndCreatedAtBetween(Long userId,
                                                             LocalDateTime startTime,
                                                             LocalDateTime endTime);

    boolean existsByUser_IdAndAlertCountedTrueAndCreatedAtBetween(Long userId,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime);

    Page<CensorRecord> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<CensorRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CensorRecord> findByUsernameContainingOrHitKeywordsContainingAllIgnoreCaseOrderByCreatedAtDesc(
            String username, String keyword, Pageable pageable);
}
