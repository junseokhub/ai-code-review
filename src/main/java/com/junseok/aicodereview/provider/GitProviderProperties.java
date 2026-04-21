package com.junseok.aicodereview.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "git.provider")
public record GitProviderProperties(
        @DefaultValue("") String githubToken,
        @DefaultValue("") String gitlabToken,
        @DefaultValue("") String bitbucketToken,
        @DefaultValue("") String codecommitAccessKey,
        @DefaultValue("") String codecommitSecretKey,
        @DefaultValue("ap-northeast-2") String codecommitRegion
) {}