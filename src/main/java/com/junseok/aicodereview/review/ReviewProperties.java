package com.junseok.aicodereview.review;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "review")
public record ReviewProperties(
        List<String> skipPatterns
) {}