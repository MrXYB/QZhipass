package org.microsoft.qintelipass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

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

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
