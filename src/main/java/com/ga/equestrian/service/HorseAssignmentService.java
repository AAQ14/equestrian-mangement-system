package com.ga.equestrian.service;
import com.ga.equestrian.dto.request.HorseAssignmentRequest;
import com.ga.equestrian.dto.request.HorseAssignmentUpdateRequest;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidBookingException;
import com.ga.equestrian.model.entity.*;
import com.ga.equestrian.model.enums.AssignmentStatus;
import com.ga.equestrian.model.enums.BookingStatus;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.repository.BookingRepository;
import com.ga.equestrian.repository.HorseAssignmentRepository;
import com.ga.equestrian.repository.HorseRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles assigning horses to bookings and changing them.
 * A lock per horse makes sure that only one thread at a time can take the same horse.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HorseAssignmentService {
    private final BookingRepository bookingRepository;
    private final HorseRepository horseRepository;
    private final HorseAssignmentRepository horseAssignmentRepository;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> assignsLock = new ConcurrentHashMap<>();
    private final TransactionTemplate transactionTemplate;

    /**
     * Assigns a horse to a booking. A lock on the horse makes sure only one thread at a time
     * can assign it, and the lock is released only after the transaction has committed.
     *
     * @param email the email of the signed-in user.
     * @param bookingId the id of the booking.
     * @param horseAssignmentRequest the request holding the id of the horse.
     * @return the created assignment.
     */
    public HorseAssignment assignHorse(String email, Long bookingId, HorseAssignmentRequest horseAssignmentRequest){
        Long horseId = horseAssignmentRequest.getHorseId();
        ReentrantLock lock = assignsLock.computeIfAbsent(horseId, id ->new ReentrantLock());

        lock.lock();
        try{
            return transactionTemplate.execute(status -> assignHorseProcess(email, bookingId, horseAssignmentRequest));
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    /**
     * Assigns a horse to a booking inside a database transaction.
     * The horse row is locked in the database, and the horse must not be assigned to another session
     * that overlaps in time.
     *
     * @param email the email of the signed-in user.
     * @param bookingId the id of the booking.
     * @param horseAssignmentRequest the request holding the id of the horse.
     * @return the created assignment.
     * @throws InformationNotFoundException if the booking or the horse does not exist.
     * @throws AccessDeniedException if the user is neither the booking's owner nor the session's instructor.
     * @throws InvalidBookingException if the booking or session is not active, or the horse is not available.
     * @throws InformationExistException if the booking already has a horse, or the horse is busy at that time.
     */
    private HorseAssignment assignHorseProcess(String email, Long bookingId,HorseAssignmentRequest horseAssignmentRequest){
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new InformationNotFoundException("Booking with this id " + bookingId + " not found")
        );
        Long horseId = horseAssignmentRequest.getHorseId();
        log.info("Horse assignment started: booking={}, horse={}, thread={}",
                bookingId, horseId, Thread.currentThread().getName());

        if(booking.getBookingStatus().equals(BookingStatus.CANCELLED)) {
            throw new InvalidBookingException("A horse cannot to be assigned to a cancelled booking.");
        }

        RidingSession ridingSession = booking.getRidingSession();
        if(ridingSession.getSessionStatus().equals(SessionStatus.COMPLETED) || ridingSession.getSessionStatus().equals(SessionStatus.CANCELLED)){
            throw new InvalidBookingException("A horse cannot be assigned to cancelled or completed session.");
        }

        Horse horse = horseRepository.findById(horseId).orElseThrow(
                ()-> new InformationNotFoundException("Horse with this id " + horseId + " not found")
        );

        if(!horse.getHorseStatus().equals(HorseStatus.AVAILABLE)){
            throw new InvalidBookingException("This horse is not available. ");
        }

        List<HorseAssignment> sameDay = horseAssignmentRepository.findAllByHorseIdAndAssignmentStatusAndRidingSessionDate(horseId, AssignmentStatus.ASSIGNED, booking.getRidingSession().getDate());

        boolean unavailable = sameDay.stream().anyMatch(a-> a.getRidingSession().getStartTime().isBefore(ridingSession.getEndTime())
        && a.getRidingSession().getEndTime().isAfter(ridingSession.getStartTime()));

        if(unavailable) {
            log.warn("Horse assignment conflict: horse={} is busy, thread={}", horseId, Thread.currentThread().getName());
            throw new InformationExistException("This horse is already assigned to another session at this time. ");
        }

        HorseAssignment assignment = new HorseAssignment();
        assignment.setBooking(booking);
        assignment.setHorse(horse);
        assignment.setRidingSession(ridingSession);
        log.info("Horse assignment succeeded: booking={}, horse={}, session={}, thread={}", bookingId, horseId, ridingSession, Thread.currentThread().getName());
        horseAssignmentRepository.save(assignment);
        return assignment;
    }

    /**
     * Gets the horse assignments of the signed-in user's bookings.
     *
     * @param email the email of the signed-in user.
     * @return the user's assignments, empty if there are none.
     * @throws InformationNotFoundException if the user does not exist.
     */
    public List<HorseAssignment> getHorseAssignments(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email " + email + " not found")
        );

        List<HorseAssignment> assignments = horseAssignmentRepository.findAllByBookingUserId(user.getId());
        return assignments;
    }

    /**
     * Changes the horse of a booking. A lock on the new horse makes sure only one thread at a time
     * can take that horse, and the lock is released only after the transaction has committed.
     *
     * @param email the email of the signed-in user.
     * @param bookingId the id of the booking.
     * @param horseAssignmentUpdateRequest the request holding the id of the new horse.
     * @return the updated assignment.
     */
    public HorseAssignment updateHorseAssignment(String email, Long bookingId, HorseAssignmentUpdateRequest horseAssignmentUpdateRequest){
        Long horseId = horseAssignmentUpdateRequest.getHorseId();
        ReentrantLock lock = assignsLock.computeIfAbsent(horseId, id ->new ReentrantLock());

        lock.lock();
        try{
            return transactionTemplate.execute(status -> updateHorseAssignmentProcess(email, bookingId, horseAssignmentUpdateRequest));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Moves a booking's assignment to a different horse inside a database transaction.
     *
     * @param email the email of the signed-in user.
     * @param bookingId the id of the booking.
     * @param horseAssignmentUpdateRequest the request holding the id of the new horse.
     * @return the updated assignment.
     * @throws InformationNotFoundException if the booking has no assignment, or the horse does not exist.
     * @throws AccessDeniedException if the booking belongs to another user.
     * @throws InvalidBookingException if the booking or session is not active, or the horse is not available.
     * @throws InformationExistException if it is already the booking's horse, or the new horse is busy at that time.
     */
    private HorseAssignment updateHorseAssignmentProcess(String email, Long bookingId, HorseAssignmentUpdateRequest horseAssignmentUpdateRequest) {
        Long horseId = horseAssignmentUpdateRequest.getHorseId();
        log.info("Horse change started: booking={}, newHorse={}, thread={}",
                bookingId, horseId, Thread.currentThread().getName());

        HorseAssignment horseAssignment = horseAssignmentRepository.findByBookingId(bookingId).orElseThrow(
                () -> new InformationNotFoundException("Booking " + bookingId + " has no horse assigned.")
        );

        Booking booking = horseAssignment.getBooking();

        BookingStatus bookingStatus = booking.getBookingStatus();
        if (bookingStatus == BookingStatus.CANCELLED || bookingStatus == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("A horse cannot be changed on a cancelled or completed booking.");
        }

        RidingSession ridingSession = booking.getRidingSession();
        if(ridingSession.getSessionStatus().equals(SessionStatus.COMPLETED) || ridingSession.getSessionStatus().equals(SessionStatus.CANCELLED)){
            throw new InvalidBookingException("A horse cannot be assigned to cancelled or completed session.");
        }

        if (horseAssignment.getHorse().getId().equals(horseId)) {
            throw new InformationExistException("This horse is already assigned to your booking.");
        }

        // database lock on the new horse, the second thing behind ReentrantLock
        Horse horse = horseRepository.findByIdForUpdate(horseId).orElseThrow(
                ()-> new InformationNotFoundException("Horse with this id " + horseId + " not found")
        );

        if (horse.getHorseStatus() != HorseStatus.AVAILABLE) {
            throw new InvalidBookingException("This horse is not available.");
        }

        List<HorseAssignment> sameDay = horseAssignmentRepository.findAllByHorseIdAndAssignmentStatusAndRidingSessionDate(horseId, AssignmentStatus.ASSIGNED, booking.getRidingSession().getDate());

        boolean unavailable = sameDay.stream().anyMatch(a-> a.getRidingSession().getStartTime().isBefore(ridingSession.getEndTime())
                && a.getRidingSession().getEndTime().isAfter(ridingSession.getStartTime()));

        if(unavailable) {
            log.warn("Horse assignment conflict: horse{} is busy, thread{}", horseId, Thread.currentThread().getName());
            throw new InformationExistException("This horse is already assigned to another session at this time. ");
        }

        if(!horseAssignment.getBooking().getUser().getEmail().equals(email)){
           throw new AccessDeniedException("You can only update your own horse assignment");
        }

        horseAssignment.setHorse(horse);
        HorseAssignment saved = horseAssignmentRepository.save(horseAssignment);
        log.info("Horse change succeeded: booking={}, newHorse={}, thread={}",
                bookingId, horseId, Thread.currentThread().getName());
        return saved;
    }
}
