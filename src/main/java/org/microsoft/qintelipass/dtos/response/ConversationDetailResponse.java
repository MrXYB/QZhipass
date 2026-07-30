package org.microsoft.qintelipass.dtos.response;

import java.util.List;

public record ConversationDetailResponse(
        ConversationResponse conversation,
        List<ConversationMessageResponse> messages,
        ModelResponse model
) {
}
