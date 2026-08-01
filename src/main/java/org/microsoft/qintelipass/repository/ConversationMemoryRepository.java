package org.microsoft.qintelipass.repository;

import org.microsoft.qintelipass.entity.ConversationMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ConversationMemoryRepository extends JpaRepository<ConversationMemory, Long> {
    @Modifying
    @Query("DELETE FROM ConversationMemory m")
    void deleteAllInBulk();
}
