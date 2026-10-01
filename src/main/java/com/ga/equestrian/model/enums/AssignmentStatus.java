package com.ga.equestrian.model.enums;

/**
 * The state of a horse assignment to a client.
 */
public enum AssignmentStatus {
    /** The horse is currently reserved for this session.*/
    ASSIGNED,
    /** The assignment was withdrawn, the horse is free again for that session.*/
    CANCELLED
}
