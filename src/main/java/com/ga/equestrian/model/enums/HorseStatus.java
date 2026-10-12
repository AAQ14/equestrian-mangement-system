package com.ga.equestrian.model.enums;

/**
 * Current state of a horse, Only AVAILABLE horses can be assigned to a booking.
 */
public enum HorseStatus {
    /** Ready to be assigned to riders. */
    AVAILABLE,
    /** Temporarily unavailable horse, could be injured; not assignable*/
    UNAVAILABLE,
    /** In training horse; not assignable. */
    IN_TRAINING,
    /** Resting horse; not assignable. */
    RESTING,
    /** No longer available for training; not assignable.*/
    RETIRED
}
