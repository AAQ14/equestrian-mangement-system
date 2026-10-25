package com.ga.equestrian.exception;

/**
 * Thrown when a ticket cannot be cancelled, for example because its tournament has already started.
 */
public class TicketCancellationNotAllowedException extends RuntimeException {
    public TicketCancellationNotAllowedException(String message) {
        super(message);
    }
}
