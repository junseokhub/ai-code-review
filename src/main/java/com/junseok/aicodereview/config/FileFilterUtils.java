package com.junseok.aicodereview.config;

import java.util.List;

public class FileFilterUtils {

    private FileFilterUtils() {}

    public static boolean shouldSkip(String fileName, List<String> skipPatterns) {
        return skipPatterns.stream()
                .anyMatch(pattern -> {
                    String regex = pattern.replace("**", ".*").replace("*", "[^/]*");
                    return fileName.matches(regex);
                });
    }
}