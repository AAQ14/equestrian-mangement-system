package com.ga.equestrian.model.enums;

/**
 * Roles that decide what a user is allowed to do in the system.
 */
public enum Role {
    /** Maintains the system: manages users, horses, instructors and sessions.*/
    ADMIN,
    /** Creates and manages their own riding sessions. */
    INSTRUCTOR,
    /** Browses and books riding sessions. */
    CLIENT
}
