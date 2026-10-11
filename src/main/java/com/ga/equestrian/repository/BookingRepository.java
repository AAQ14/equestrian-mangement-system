package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for reading and writing client bookings of riding sessions.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Counts the bookings of a session that have one of the given statuses.
     * Used to check how many spots are taken, so pass the statuses that hold a spot.
     *
     * @param sessionId the id of the riding session.
     * @param bookingStatusList the statuses that count as a taken spot.
     * @return the number of matching bookings, or 0 if there are none.
     */
    long countByRidingSessionIdAndBookingStatusIn(Long sessionId, List<BookingStatus> bookingStatusList);

    /**
     * Checks whether a user already has a booking for a session whose status is not the given one.
     * Passing CANCELLED lets a user book again after cancelling.
     *
     * @param userId the id of the user.
     * @param sessionId the id of the riding session.
     * @param bookingStatus the status to exclude.
     * @return true if such a booking exists, false otherwise.
     */
    boolean existsByUserIdAndRidingSessionIdAndBookingStatusNot(Long userId, Long sessionId, BookingStatus bookingStatus);

    /**
     * Finds all the bookings that belong to a user.
     *
     * @param userId the id of the user.
     * @return the user's bookings, or an empty list if there are none.
     */
    List<Booking> findByUserId(Long userId);

}
