package com.ga.equestrian.exception;

/**
 * Thrown when an action on a riding session breaks a business rule,
 * such as cancelling a session that is already cancelled or completed.
 */
public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String message) {
        super(message);
    }
}
