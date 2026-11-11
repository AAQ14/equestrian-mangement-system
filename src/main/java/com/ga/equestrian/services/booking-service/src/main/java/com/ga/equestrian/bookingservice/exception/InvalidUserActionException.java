package com.ga.equestrian.bookingservice.exception;

public class InvalidUserActionException extends RuntimeException {
    public InvalidUserActionException(String message) {
        super(message);
    }
}
