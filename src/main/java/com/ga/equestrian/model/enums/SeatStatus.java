package com.ga.equestrian.model.enums;

/**
 * Represents whether a seat can still be bought.
 */
public enum SeatStatus {
    /** The seat is free to buy. */
    AVAILABLE,

    /** The seat is unavailable and has an active ticket. */
    SOLD
}
