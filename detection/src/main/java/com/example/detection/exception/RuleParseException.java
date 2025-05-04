package com.example.detection.exception;

/**
 * Exception thrown when there is an error parsing a rule.
 */
public class RuleParseException extends RuntimeException {

    public RuleParseException(String message) {
        super(message);
    }

    public RuleParseException(String message, Throwable cause) {
        super(message, cause);
    }
}