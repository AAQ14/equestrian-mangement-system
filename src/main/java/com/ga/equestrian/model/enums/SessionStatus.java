package com.ga.equestrian.model.enums;

/**
 * The state of the session.
 */
public enum SessionStatus {
    /** Open for booking. */
    AVAILABLE,
    /** Capacity reached.*/
    FULL,
    /** Canceled by the instructor or admin.*/
    CANCELLED,
    /** The session has finished.*/
    COMPLETED
}
