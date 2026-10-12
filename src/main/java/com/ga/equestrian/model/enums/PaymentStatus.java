package com.ga.equestrian.model.enums;

/**
 * The state of payment for a booking.
 */
public enum PaymentStatus {
    /** The payment has been created and is waiting to be paid. */
    PENDING,
    /** The payment succeeded, so the booking can be confirmed. */
    COMPLETED,
    /** The payment not succeeded, so the booking can not be confirmed. */
    FAILED,
    /** The money was returned to the client(e.g. after cancellation). */
    REFUNDED
}
