package com.ga.equestrian.exception;

/**
 * Thrown when a booking breaks a business rule, such as booking a past or unavailable session.
 */
public class InvalidBookingException extends RuntimeException{
    public InvalidBookingException(String message){
        super(message);
    }
}
