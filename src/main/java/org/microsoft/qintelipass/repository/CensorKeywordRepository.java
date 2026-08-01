package org.microsoft.qintelipass.repository;


import org.microsoft.qintelipass.entity.CensorKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
public interface CensorKeywordRepository extends JpaRepository<CensorKeyword, Long>,
        JpaSpecificationExecutor<CensorKeyword> {

    List<CensorKeyword> findByEnabledTrue();
    List<CensorKeyword> findAllByOrderByCreatedAtDesc();
    Optional<CensorKeyword> findByKeyword(String keyword);
    boolean existsByKeyword(String keyword);
}
