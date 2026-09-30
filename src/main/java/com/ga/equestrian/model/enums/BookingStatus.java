package com.ga.equestrian.model.enums;

/**
 * The state of the booking.
 * PENDING and CONFIRMED hold a seat in the session.
 * A CANCELLED status releases the seat.
 */
public enum BookingStatus {
    /** Booking waiting for payment. */
    PENDING,
    /** Session booked, paid and confirmed. */
    CONFIRMED,
    /** Booking canceled by the client, or if the instructor canceled that session.*/
    CANCELLED,
    /** The client attended the session.*/
    COMPLETED
}
