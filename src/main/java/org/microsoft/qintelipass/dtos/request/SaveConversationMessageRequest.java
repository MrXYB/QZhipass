package org.microsoft.qintelipass.dtos.request;

import lombok.Data;

@Data
public class SaveConversationMessageRequest {
    private String role;
    private String content;
    private String modelKey;
}
