package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.UserAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAgentRepository extends JpaRepository<UserAgent, Long> {

    List<UserAgent> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Optional<UserAgent> findByIdAndUserIdAndStatus(Long id, Long userId, String status);

    Optional<UserAgent> findByUserIdAndNameAndStatus(Long userId, String name, String status);

    long countByUserIdAndStatus(Long userId, String status);
}
