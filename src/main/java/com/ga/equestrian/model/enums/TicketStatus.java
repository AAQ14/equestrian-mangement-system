package com.ga.equestrian.model.enums;

/**
 * Represents the status of a ticket.
 */
public enum TicketStatus {
    /** The seat is revered and the payment is not done yet.*/
    PENDING,

    /** The payment is done and the ticket is valid. */
    CONFIRMED,

    /** The ticket was cancelled and the seat is available again.*/
    CANCELLED
}
