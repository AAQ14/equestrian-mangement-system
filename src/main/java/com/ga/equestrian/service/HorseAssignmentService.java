package com.ga.equestrian.service;
import com.ga.equestrian.dto.request.HorseAssignmentRequest;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidBookingException;
import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.entity.Horse;
import com.ga.equestrian.model.entity.HorseAssignment;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.enums.AssignmentStatus;
import com.ga.equestrian.model.enums.BookingStatus;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.repository.BookingRepository;
import com.ga.equestrian.repository.HorseAssignmentRepository;
import com.ga.equestrian.repository.HorseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Horse service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HorseAssignmentService {
    private final BookingRepository bookingRepository;
    private final HorseRepository horseRepository;
    private final HorseAssignmentRepository horseAssignmentRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> assignsLock = new ConcurrentHashMap<>();
    private final TransactionTemplate transactionTemplate;

    public HorseAssignment assignHorse(String email, HorseAssignmentRequest horseAssignmentRequest){
        Long horseId = horseAssignmentRequest.getHorseId();
        ReentrantLock lock = assignsLock.computeIfAbsent(horseId, id ->new ReentrantLock());

        lock.lock();
        try{
            return transactionTemplate.execute(status -> assignHorseProcess(email, horseAssignmentRequest));
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    private HorseAssignment assignHorseProcess(String email, HorseAssignmentRequest horseAssignmentRequest){
        Long bookingId = horseAssignmentRequest.getBookingId();
        Long horseId = horseAssignmentRequest.getHorseId();
        log.info("Horse assignment started: booking={}, horse={}, thread={}",
                bookingId, horseId, Thread.currentThread().getName());


        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new InformationNotFoundException("Booking with this id " + bookingId + " not found")
        );

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
            log.warn("Horse assignment conflict: horse{} is busy, thread{}", horseId, Thread.currentThread().getName());
            throw new InformationExistException("This horse is already assigned to another session at this time. ");
        }

        HorseAssignment assignment = new HorseAssignment();
        assignment.setBooking(booking);
        assignment.setHorse(horse);
        assignment.setRidingSession(ridingSession);
        log.info("Horse assignment succeeded: booking{}, horse{}, session{}, thread{}", bookingId, horseId, ridingSession, Thread.currentThread().getName());
        horseAssignmentRepository.save(assignment);
        return assignment;
    }
}
