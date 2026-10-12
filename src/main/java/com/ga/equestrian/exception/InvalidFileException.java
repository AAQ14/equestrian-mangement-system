package com.ga.equestrian.exception;

/**
 * Thrown when a file uploaded is invalid.
 */
public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}
