package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.BookingResponse;
import com.ga.equestrian.model.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Converts a booking entity into a response.
 */
@Component
public class BookingMapper {
    /**
     * Converts a booking entity into its DTO response.
     * @param booking the booking entity.
     * @return the booking response.
     */
    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getUser().getId(),
                booking.getRidingSession().getId(), booking.getBookingStatus(),
                booking.getCreatedAt(), booking.getUpdatedAt());
    }
}
