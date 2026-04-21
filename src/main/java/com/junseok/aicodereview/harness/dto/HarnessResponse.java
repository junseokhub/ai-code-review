package com.junseok.aicodereview.harness.dto;

public record HarnessResponse(
        boolean success,
        String provider,
        String topic,
        String query,
        String answer,
        boolean blocked,
        String blockedReason,
        TokenUsage tokenUsage
) {
    public record TokenUsage(
            int inputTokens,
            int outputTokens,
            int totalTokens
    ) {}
}