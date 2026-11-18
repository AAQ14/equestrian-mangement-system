package com.ga.equestrian.userservice.exception;

/**
 * Thrown when a verification code is wrong, expired, or locked after too many attempts.
 */
public class InvalidCodeException extends RuntimeException {
    public InvalidCodeException(String message) {
        super(message);
    }
}
