package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.UserAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAgentRepository extends JpaRepository<UserAgent, Long> {

    List<UserAgent> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    Optional<UserAgent> findByIdAndUser_IdAndStatus(Long id, Long userId, String status);

    Optional<UserAgent> findByUser_IdAndNameAndStatus(Long userId, String name, String status);

    long countByUser_IdAndStatus(Long userId, String status);
}
