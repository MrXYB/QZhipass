package org.microsoft.qintelipass.dtos.response;

public record ConversationTurnResponse(
        ConversationResponse conversation,
        ConversationMessageResponse userMessage,
        ConversationMessageResponse assistantMessage,
        int contextTokens,
        int providerPromptTokens,
        int providerCompletionTokens
) {
}
