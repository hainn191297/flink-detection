package com.example.detection.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuleAction {
    ADD,
    UPDATE,
    DELETE;

    // Cho phép deserialize từ lowercase hoặc uppercase string
    @JsonCreator
    public static RuleAction fromString(String value) {
        return value == null ? null : RuleAction.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
