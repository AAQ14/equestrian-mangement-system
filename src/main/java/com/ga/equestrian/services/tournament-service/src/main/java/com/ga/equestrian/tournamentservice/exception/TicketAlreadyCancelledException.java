package com.ga.equestrian.tournamentservice.exception;

public class TicketAlreadyCancelledException extends RuntimeException {
    public TicketAlreadyCancelledException(String message) {
        super(message);
    }
}
