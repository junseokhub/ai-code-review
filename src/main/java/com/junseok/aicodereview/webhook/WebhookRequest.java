package com.junseok.aicodereview.webhook;

import com.junseok.aicodereview.harness.LLMProvider;
import com.junseok.aicodereview.provider.GitProviderType;

public record WebhookRequest(
        GitProviderType providerType,
        LLMProvider llmProvider,
        String repoName,
        Integer prNumber,
        String commitSha
) {}