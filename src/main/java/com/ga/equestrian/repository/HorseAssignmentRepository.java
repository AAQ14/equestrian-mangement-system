package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.HorseAssignment;
import com.ga.equestrian.model.enums.AssignmentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for reading and saving horse assignments.
 */
public interface HorseAssignmentRepository extends JpaRepository<HorseAssignment, Long> {
    /**
     * Checks whether a booking already has a horse.
     *
     * @param bookingId the id of the booking.
     * @return true if an assignment exists.
     */
    boolean existsByBookingId(Long bookingId);

    /**
     * Finds the assignments of a horse with a given status and a date.
     * Used to check weather the horse is already busy at the same time of another session.
     *
     * @param horseId the id of the horse.
     * @param assignmentStatus the status of the assignments, such as ASSIGNED.
     * @param date the date of the riding sessions.
     * @return the matching assignments, empty if there are none.
     */
    List<HorseAssignment> findAllByHorseIdAndAssignmentStatusAndRidingSessionDate(Long horseId, AssignmentStatus assignmentStatus, LocalDate date);


    List<HorseAssignment> findAllByUserId(Long userId);

    /**
     * Finds the assignment of a booking.
     *
     * @param bookingId the id of the booking.
     * @return the assignment, if the booking has a horse.
     */

    Optional<HorseAssignment> findByBookingId(Long bookingId);

    /**
     * Finds the horse assignments of all the bookings that belong to a user.
     *
     * @param userId the id of the user.
     * @return the user's assignments, empty if there are none.
     */
    List<HorseAssignment> findAllByBookingUserId(Long userId);

}
