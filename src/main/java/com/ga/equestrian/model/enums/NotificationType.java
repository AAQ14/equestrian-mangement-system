package com.ga.equestrian.model.enums;

/**
 * The kind of event a notification is about.
 * The type tells the client what happened;
 * the notification message gives the details.
 */
public enum NotificationType {
    /**A booking was created, sent to the booked client and the session's instructor. */
    BOOKING_CREATED,
    /**An admin deactivated the account, sent to the affected user. */
    BOOKING_CONFIRMED,
    /**A booking is cancelled by client, or the session was cancelled, sent to the client*/
    BOOKING_CANCELLED,
    /** A session's date, time or price changed, sent to clients with an active booking. */
    SESSION_UPDATED,
    /** A session was cancelled, sent to clients with an active booking. */
    SESSION_CANCELLED,
    /** A general message. */
    SYSTEM
}
