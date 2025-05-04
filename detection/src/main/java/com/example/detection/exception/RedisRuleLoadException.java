package com.example.detection.exception;

public class RedisRuleLoadException extends RuntimeException {

    public RedisRuleLoadException(String message) {
        super(message);
    }

    public RedisRuleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}