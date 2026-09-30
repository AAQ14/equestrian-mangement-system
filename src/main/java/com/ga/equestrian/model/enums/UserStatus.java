package com.ga.equestrian.model.enums;

/**
 * Status of user account's lifetime.
 * Only Active users are allowed to log in.
 */
public enum UserStatus {
    /** Registered but email is not verified*/
    PENDING,
    /** Verified and allowed to use the system*/
    ACTIVE,
    /** deactivated or deleted by an admin*/
    INACTIVE
}
