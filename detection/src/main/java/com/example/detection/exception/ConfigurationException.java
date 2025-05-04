package com.example.detection.exception;

/**
 * Exception thrown when there are errors related to configuration loading or
 * processing.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}