package com.ga.equestrian.exception;

/**
 * Thrown when a client tries to buy a seat that is already sold.
 */
public class SeatAlreadySoldException extends RuntimeException {
    public SeatAlreadySoldException(String message) {
        super(message);
    }
}
