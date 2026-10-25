package com.ga.equestrian.exception;

public class TicketAlreadyCancelledException extends RuntimeException {
    public TicketAlreadyCancelledException(String message) {
        super(message);
    }
}
