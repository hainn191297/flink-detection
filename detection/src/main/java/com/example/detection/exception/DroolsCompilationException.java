package com.example.detection.exception;

/**
 * Exception thrown when there is a failure in compiling Drools rules.
 */
public class DroolsCompilationException extends RuntimeException {

    public DroolsCompilationException(String message) {
        super(message);
    }

    public DroolsCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}