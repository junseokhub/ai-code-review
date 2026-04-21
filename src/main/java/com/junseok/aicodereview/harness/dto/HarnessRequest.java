package com.junseok.aicodereview.harness.dto;

import com.junseok.aicodereview.harness.LLMProvider;
import lombok.Builder;

@Builder
public record HarnessRequest(
        LLMProvider provider,
        String model,
        String topic,
        String topicDesc,
        String query,
        String context
) {}