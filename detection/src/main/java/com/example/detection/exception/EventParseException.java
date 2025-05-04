package com.example.detection.exception;

/**
 * Exception thrown when there is an error parsing a rule.
 */
public class EventParseException extends RuntimeException {

    public EventParseException(String message) {
        super(message);
    }

    public EventParseException(String message, Throwable cause) {
        super(message, cause);
    }
}