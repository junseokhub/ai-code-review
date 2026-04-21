package com.junseok.aicodereview.provider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitProviderRegistry {

    private final Map<GitProviderType, GitProvider> providers;

    public GitProviderRegistry(List<GitProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        GitProvider::getProviderType,
                        provider -> provider
                ));
    }

    public GitProvider getProvider(GitProviderType type) {
        GitProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("지원하지 않는 provider: " + type);
        }
        return provider;
    }
}