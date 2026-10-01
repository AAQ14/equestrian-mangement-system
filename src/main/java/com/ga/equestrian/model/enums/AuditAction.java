package com.ga.equestrian.model.enums;

/**
 *  The kind of action recorded in the audit log.
 */
public enum AuditAction {
    /** The user logged into the app successfully. */
    LOGIN,
    /** The user failed to log in into the app successfully (e.g. wrong password). */
    LOGIN_FAILED,
    /** An instructor created a riding session.*/
    CREATE_RIDING_SESSION,
    /** An instructor updated a riding session. */
    UPDATE_RIDING_SESSION,
    /** A client booked a riding session. */
    CREATE_BOOKING,
    /** A client cancelled a riding session. */
    CANCEL_BOOKING,
    /** An instructor or client assigned a horse. */
    ASSIGN_HORSE,
    /** An instructor updated a horse. */
    UPDATE_HORSE,
    /** A file uploaded (e.g. a picture profile).*/
    UPLOAD_FILE,
    /** An admin deleted a user. */
    DELETE_USER
}
