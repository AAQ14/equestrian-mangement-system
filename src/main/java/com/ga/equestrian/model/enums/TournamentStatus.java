package com.ga.equestrian.model.enums;

/**
 * Represents the lifecycle status of a tournament.
 */
public enum TournamentStatus {
    /** The tournament is scheduled and tickets can be bought*/
    UPCOMING,

    /** The tournament is currently taking place. */
    ONGOING,

    /** The tournament has finished. */
    COMPLETED,

    /** The tournament was cancelled by an admin and ticket holders must be notified. . */
    CANCELLED
}
