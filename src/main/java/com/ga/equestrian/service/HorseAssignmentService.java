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
import org.springframework.transaction.annotation.Transactional;
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
            throw new InvalidBookingException("A horse cannot be assigned to a cancelled booking.");
        }

        if(booking.getBookingStatus().equals(BookingStatus.COMPLETED)) {
            throw new InvalidBookingException("A horse cannot be assigned to a completed booking.");
        }

        if(horseAssignmentRepository.existsByBookingId(bookingId)){
            throw new InformationExistException("This booking already has a horse.");
        }

        RidingSession ridingSession = booking.getRidingSession();
        if(ridingSession.getSessionStatus().equals(SessionStatus.COMPLETED) || ridingSession.getSessionStatus().equals(SessionStatus.CANCELLED)){
            throw new InvalidBookingException("A horse cannot be assigned to cancelled or completed session.");
        }

        boolean isBookingOwner = booking.getUser().getEmail().equals(email);
        boolean isInstructor = ridingSession.getInstructor().getUser().getEmail().equals(email);

        if (!isBookingOwner && !isInstructor) {
            throw new AccessDeniedException("You can only cancel the horse of your own booking or your own session.");
        }

        Horse horse = horseRepository.findByIdForUpdate(horseId).orElseThrow(
                ()-> new InformationNotFoundException("Horse with this id " + horseId + " not found")
        );

        if(!horse.getHorseStatus().equals(HorseStatus.AVAILABLE)){
            throw new InvalidBookingException("This horse is not available.");
        }

        List<HorseAssignment> sameDay = horseAssignmentRepository.findAllByHorseIdAndAssignmentStatusAndRidingSessionDate(horseId, AssignmentStatus.ASSIGNED, booking.getRidingSession().getDate());

        boolean unavailable = sameDay.stream().anyMatch(a-> a.getRidingSession().getStartTime().isBefore(ridingSession.getEndTime())
        && a.getRidingSession().getEndTime().isAfter(ridingSession.getStartTime()));

        if(unavailable) {
            log.warn("Horse assignment conflict: horse={} is busy, thread={}", horseId, Thread.currentThread().getName());
            throw new InformationExistException("This horse is already assigned to another session at this time.");
        }

        HorseAssignment assignment = new HorseAssignment();
        assignment.setBooking(booking);
        assignment.setHorse(horse);
        assignment.setRidingSession(ridingSession);
        log.info("Horse assignment succeeded: booking={}, horse={}, session={}, thread={}", bookingId, horseId, ridingSession.getId(), Thread.currentThread().getName());
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
    @Transactional(readOnly = true)
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
     * @throws AccessDeniedException  if the user is neither the booking owner nor the session's instructor.
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

        RidingSession ridingSession = booking.getRidingSession();

        boolean isBookingOwner = horseAssignment.getBooking().getUser().getEmail().equals(email);
        boolean isInstructor = ridingSession.getInstructor().getUser().getEmail().equals(email);

        if (!isBookingOwner && !isInstructor) {
            throw new AccessDeniedException("You can only update the horse of your own booking or your own session.");
        }

        if(ridingSession.getSessionStatus().equals(SessionStatus.COMPLETED) || ridingSession.getSessionStatus().equals(SessionStatus.CANCELLED)){
            throw new InvalidBookingException("A horse cannot be assigned to cancelled or completed session.");
        }

        BookingStatus bookingStatus = booking.getBookingStatus();
        if (bookingStatus == BookingStatus.CANCELLED || bookingStatus == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("A horse cannot be changed on a cancelled or completed booking.");
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
            log.warn("Horse assignment conflict: horse={} is busy, thread={}", horseId, Thread.currentThread().getName());
            throw new InformationExistException("This horse is already assigned to another session at this time.");
        }


        horseAssignment.setHorse(horse);
        HorseAssignment saved = horseAssignmentRepository.save(horseAssignment);
        log.info("Horse change succeeded: booking={}, newHorse={}, thread={}",
                bookingId, horseId, Thread.currentThread().getName());
        return saved;
    }

    /**
     * Gets the horse assignment of one booking.
     *
     * @param email the email of the signed-in user.
     * @param bookingId the id of the booking.
     * @return the assignment of the booking.
     * @throws InformationNotFoundException if the booking has no horse assigned.
     * @throws AccessDeniedException if the user is neither the booking's owner nor the session's instructor.
     */
    @Transactional(readOnly = true)
    public HorseAssignment getHorseAssignmentByBooking(String email, Long bookingId){
        HorseAssignment horseAssignment = horseAssignmentRepository.findByBookingId(bookingId).orElseThrow(
                ()->new InformationNotFoundException("Booking " + bookingId + " has no horse assigned.")
        );

        boolean isBookingOwner = horseAssignment.getBooking().getUser().getEmail().equals(email);
        boolean isInstructor = horseAssignment.getRidingSession().getInstructor().getUser().getEmail().equals(email);
        if (!isBookingOwner && !isInstructor) {
            throw new AccessDeniedException("You can only view the horse of your own booking or your own session.");
        }
        return horseAssignment;
    }

    /**
     * Removes a horse from a booking, so the horse is free again at this time.
     * The assignment row is deleted, so the booking can get a new horse later.
     *
     * @param email the email of the sign-in user.
     * @param bookingId the id of the booking.
     * @throws InformationNotFoundException if the booking has no horse assigned.
     * @throws AccessDeniedException if the user is neither the booking owner nor the session's instructor.
     * @throws InvalidBookingException if the session is already completed or cancelled.
     */
    @Transactional
    public void cancelHorseAssignment(String email, Long bookingId) {
        HorseAssignment horseAssignment = horseAssignmentRepository.findByBookingId(bookingId).orElseThrow(
                ()->new InformationNotFoundException("Booking " + bookingId + " has no horse assigned.")
        );

//        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
//                ()-> new InformationNotFoundException("booking with this id is not found.")
//        );

        RidingSession session = horseAssignment.getBooking().getRidingSession();
        boolean isBookingOwner = horseAssignment.getBooking().getUser().getEmail().equals(email);
        boolean isInstructor = session.getInstructor().getUser().getEmail().equals(email);

        if (!isBookingOwner && !isInstructor) {
            throw new AccessDeniedException("You can only cancel the horse of your own booking or your own session.");
        }

        if(session.getSessionStatus() == SessionStatus.COMPLETED) {
            throw new InvalidBookingException("The horse of a completed session cannot be cancelled.");
        }

        if(session.getSessionStatus() == SessionStatus.CANCELLED) {
            throw new InvalidBookingException("The session is cancelled already.");
        }

        horseAssignmentRepository.delete(horseAssignment);
//        booking.setHorseAssignment(null);
//        bookingRepository.save(booking);

        log.info("Horse assignment cancelled: booking={}, horse={}, thread={}",
                bookingId, horseAssignment.getHorse().getId(), Thread.currentThread().getName());
    }
}
