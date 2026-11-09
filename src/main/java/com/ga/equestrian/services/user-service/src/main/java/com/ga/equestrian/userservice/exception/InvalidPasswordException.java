package com.ga.equestrian.userservice.exception;

/**
 * Thrown when a password change is rejected, such as a wrong current password.
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
