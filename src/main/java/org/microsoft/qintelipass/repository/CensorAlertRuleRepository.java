package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.models.CensorAlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CensorAlertRuleRepository extends JpaRepository<CensorAlertRule, Long> {
    List<CensorAlertRule> findByEnabledTrueOrderByCreatedAtAsc();
    List<CensorAlertRule> findAllByOrderByUpdatedAtDesc();
    Optional<CensorAlertRule> findFirstByDefaultRuleTrue();
}
