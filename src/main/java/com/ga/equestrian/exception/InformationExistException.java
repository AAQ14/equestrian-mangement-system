package com.ga.equestrian.exception;

/**
 * Thrown when an information already exists.
 */
public class InformationExistException extends RuntimeException {
    public InformationExistException(String message) {
        super(message);
    }
}
