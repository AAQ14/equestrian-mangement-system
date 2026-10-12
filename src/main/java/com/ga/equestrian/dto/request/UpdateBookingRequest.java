package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON that clients send to change the status of a booking.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateBookingRequest {
    @NotNull(message = "Booking status is required")
    private BookingStatus bookingStatus;
}
