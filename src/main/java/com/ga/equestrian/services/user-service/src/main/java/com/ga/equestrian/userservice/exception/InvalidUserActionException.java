package com.ga.equestrian.userservice.exception;

public class InvalidUserActionException extends RuntimeException {
    public InvalidUserActionException(String message) {
        super(message);
    }
}
