package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.models.PublicAgentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicAgentTemplateRepository extends JpaRepository<PublicAgentTemplate, Long> {

    List<PublicAgentTemplate> findByStatusOrderByCreatedAtAsc(String status);
}
