package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.hotkey.Function;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FunctionKeyRepository extends JpaRepository<Function, Integer> {
}
