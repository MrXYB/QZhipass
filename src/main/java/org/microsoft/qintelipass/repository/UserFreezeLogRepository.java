package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.UserFreezeLog;
import org.microsoft.qintelipass.enums.UserFreezeAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFreezeLogRepository extends JpaRepository<UserFreezeLog, Long> {
    List<UserFreezeLog> findByUser_IdOrderByOperatedAtDesc(Long userId);

    Optional<UserFreezeLog> findFirstByUser_IdAndActionOrderByOperatedAtDesc(
            Long userId,
            UserFreezeAction action
    );
}