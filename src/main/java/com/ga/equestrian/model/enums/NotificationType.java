package com.ga.equestrian.model.enums;

/**
 * The kind of event a notification is about.
 * The type tells the client what happened;
 * the notification message gives the details.
 */
public enum NotificationType {
    /**An admin deactivated the account, sent to the affected user. */
    BOOKING_CONFIRMED,
    /**A booking is cancelled by client, or the session was cancelled, sent to the client*/
    BOOKING_CANCELLED,
    /** A payment was completed successfully, sent to the client*/
    PAYMENT_COMPLETED,
    /** A payment did not go through, sent to the client.*/
    PAYMENT_FAILED,
    /** A payment was refunded, (e.g. after cancellation), sent to the client.*/
    PAYMENT_REFUNDED,
    /** A horse assignment to a booking, sent to the client. */
    HORSE_ASSIGNED,
    /** A horse assignment was withdrawn, sent to the client. */
    HORSE_ASSIGNMENT_CANCELLED,
    /** A general message. */
    SYSTEM
}
