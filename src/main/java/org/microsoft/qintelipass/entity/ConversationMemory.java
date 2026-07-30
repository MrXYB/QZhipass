package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "conversation_memories")
public class ConversationMemory {
    @Id
    @Column(name = "conversation_id")
    private Long conversationId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Lob
    @Column(name = "summary_content", nullable = false, columnDefinition = "LONGTEXT")
    private String summaryContent = "";

    @Column(name = "summary_token_count", nullable = false)
    private int summaryTokenCount;

    @Column(name = "summarized_through_message_id")
    private Long summarizedThroughMessageId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
