package com.junseok.aicodereview.harness;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LLMProvider {
    CLAUDE("claude"),
    OPENAI("openai");

    private final String value;

    LLMProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static LLMProvider from(String value) {
        for (LLMProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(value) || provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 LLM provider: " + value);
    }
}