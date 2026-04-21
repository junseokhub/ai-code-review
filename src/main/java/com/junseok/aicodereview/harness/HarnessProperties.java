package com.junseok.aicodereview.harness;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "harness")
public record HarnessProperties(
        String url
) {}