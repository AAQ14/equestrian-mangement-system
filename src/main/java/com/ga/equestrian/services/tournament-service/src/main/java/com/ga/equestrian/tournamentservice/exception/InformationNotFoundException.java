package com.ga.equestrian.tournamentservice.exception;

/**
 * Thrown when requested information is not found.
 */
public class InformationNotFoundException extends RuntimeException {
    public InformationNotFoundException(String message) {
        super(message);
    }
}
