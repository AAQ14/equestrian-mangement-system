package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.BookingRequest;
import com.ga.equestrian.dto.request.UpdateBookingRequest;
import com.ga.equestrian.dto.response.BookingResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidBookingException;
import com.ga.equestrian.mapper.BookingMapper;
import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.*;
import com.ga.equestrian.repository.BookingRepository;
import com.ga.equestrian.repository.RidingSessionRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {
    private static final List<BookingStatus> SEAT_HOLDING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RidingSessionRepository ridingSessionRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    @Transactional
    public BookingResponse createBooking(String email, BookingRequest request){
        User signedInUser = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with email " + email + " not found.")
        );

        User bookedUser = signedInUser;

        if(request.getUserId()!=null) {
            if (signedInUser.getRole() == Role.CLIENT) {
                throw new AccessDeniedException("Clients can only book for themselves.");
            }
            bookedUser = userRepository.findById(request.getUserId()).orElseThrow(
                    () -> new InformationNotFoundException("User with id " + request.getUserId() + " not found.")
            );
        }
            if(bookedUser.getUserStatus() != UserStatus.ACTIVE){
                throw new InvalidBookingException("Bookings can only be made for active users.");
            }
            RidingSession session = ridingSessionRepository.findById(request.getRidingSessionId()).orElseThrow(
                    () -> new InformationNotFoundException("Riding session with id " + request.getRidingSessionId() + " not found.")
            );
            if (session.getSessionStatus() != SessionStatus.AVAILABLE) {
                throw new InvalidBookingException("This session is not available for booking.");
            }

            LocalDateTime sessionStart = LocalDateTime.of(session.getDate(), session.getStartTime());
            if (sessionStart.isBefore(LocalDateTime.now())) {
                throw new InvalidBookingException("This session has already started or finished.");
            }

            if(bookingRepository.existsByUserIdAndRidingSessionIdAndBookingStatusNot(bookedUser.getId(), session.getId(), BookingStatus.CANCELLED)){
                throw new InformationExistException("This user already has a booking for this session.");
            }

             if(bookingRepository.existsByUserIdAndRidingSessionDateAndRidingSessionStartTimeLessThanAndRidingSessionEndTimeGreaterThanAndBookingStatusIn(
                bookedUser.getId(), session.getDate(), session.getEndTime(), session.getStartTime(), SEAT_HOLDING_STATUSES)){
                 throw new InformationExistException("This user already has a booking that overlaps this session.");
             }

            long takenSpots = bookingRepository.countByRidingSessionIdAndBookingStatusIn(session.getId(), SEAT_HOLDING_STATUSES);
            if(takenSpots>= session.getCapacity()){
                throw new InformationExistException("Riding session with id " + session.getId() + " is fully booked.");
            }

            Booking booking = new Booking();
            booking.setUser(bookedUser);
            booking.setRidingSession(session);
            Booking savedBooking = bookingRepository.save(booking);
            if (takenSpots + 1 >= session.getCapacity()) {
                session.setSessionStatus(SessionStatus.FULL);
            }
        log.info("User {} created booking {} for session {} (booked user {})",
                signedInUser.getId(), savedBooking.getId(), session.getId(), bookedUser.getId());

            notificationService.createAndSend(bookedUser,  "Your booking for " + session.getDate() + " was created and is pending confirmation.", NotificationType.BOOKING_CREATED);
            return bookingMapper.toResponse(savedBooking);

    }

    /**
     * Returns bookings as response DTOs, optionally filtered by status.
     * Clients get only their own bookings; instructors and admins get all of them.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @param status the booking status to filter by, or null for all statuses.
     * @return the list of bookings, empty if there are none.
     * @throws InformationNotFoundException if the signed-in user does not exist.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(String email, BookingStatus status) {
        User signedInUser = findUserByEmail(email);
        List<Booking> bookings;
        if(signedInUser.getRole() == Role.CLIENT){
            if(status != null){
                bookings = bookingRepository.findByUserIdAndBookingStatus(signedInUser.getId(), status);
            } else{
                bookings = bookingRepository.findByUserId(signedInUser.getId());
            }
        } else{
            if(status != null){
                bookings = bookingRepository.findByBookingStatus(status);
            }else{
                bookings = bookingRepository.findAll();
            }
        }
        return bookings.stream().map(bookingMapper::toResponse).toList();
    }



    /**
     * Finds a user by the email stored in the JWT.
     *
     * @param email the user's email.
     * @return the user.
     * @throws InformationNotFoundException if no user has this email.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new InformationNotFoundException("User with email " + email + " not found."));
    }


    /**
     * Returns one booking by id.
     * Clients can only view their own bookings.
     *
     * @param id the booking id.
     * @param email the email of the signed-in user, taken from the JWT.
     * @return the booking as a response DTO.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client requests another user's booking.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, String email){
        User signedInUser = findUserByEmail(email);
        return bookingMapper.toResponse(findAccessibleBooking(id, signedInUser));
    }

    /**
     * Changes the status of a booking, following the allowed transitions.
     * Clients can only cancel their own bookings; instructors and admins can apply any allowed change.
     *
     * @param id the booking id.
     * @param email the email of the signed-in user, taken from the JWT.
     * @param request  the new status.
     * @return the updated booking as a response DTO.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client tries to change another user's booking or to set a status other than CANCELLED.
     * @throws InvalidBookingException if the status change is not allowed.
     */
    @Transactional
    public BookingResponse updateBooking(Long id, String email, UpdateBookingRequest request){
        User signedInUser = findUserByEmail(email);
        Booking booking = findAccessibleBooking(id, signedInUser);
        if(signedInUser.getRole() == Role.CLIENT && request.getBookingStatus() != BookingStatus.CANCELLED){
            throw new AccessDeniedException("Clients can only cancel their bookings.");
        }
        changeStatus(booking, request.getBookingStatus());
        log.info("User {} changed booking {} to {}", signedInUser.getId(), booking.getId(), booking.getBookingStatus());
        return bookingMapper.toResponse(booking);
    }

    /**
     * Cancels a booking by id. The booking is kept in the database with the CANCELLED status,
     * and its spot in the session is released.
     *
     * @param id the booking id.
     * @param email the email of the signed-in user, taken from the JWT.
     * @return the cancelled booking.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client cancels another user's booking.
     * @throws InvalidBookingException if the booking is already cancelled or completed.
     */
    @Transactional
    public BookingResponse deleteBooking(Long id, String email){
        User signedInUser = findUserByEmail(email);
        Booking booking = findAccessibleBooking(id, signedInUser);
        changeStatus(booking, BookingStatus.CANCELLED);
        log.info("User {} cancelled booking {}", signedInUser.getId(), booking.getId());
        return bookingMapper.toResponse(booking);
    }

    /**
     * Finds a booking and checks that the signed-in user is allowed to access it.
     *
     * @param id the booking id.
     * @param signedInUser the user making the request.
     * @return the booking.
     * @throws InformationNotFoundException if no booking has this id.
     * @throws AccessDeniedException if a client asks for another user's booking.
     */
    private Booking findAccessibleBooking(Long id, User signedInUser) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Booking with id " + id + " not found."));
        if (signedInUser.getRole() == Role.CLIENT && !booking.getUser().getId().equals(signedInUser.getId())) {
            throw new AccessDeniedException("You can only access your own bookings.");
        }
        return booking;
    }


    private void changeStatus(Booking booking, BookingStatus newStatus){
        BookingStatus currentStatus = booking.getBookingStatus();

        if(currentStatus == BookingStatus.PENDING){
            if(newStatus != BookingStatus.CONFIRMED && newStatus!=BookingStatus.CANCELLED) {
                throw new InvalidBookingException("Invalid booking status change.");
            }
        }else if (currentStatus == BookingStatus.CONFIRMED) {
            if (newStatus != BookingStatus.COMPLETED &&
                    newStatus != BookingStatus.CANCELLED) {
                throw new InvalidBookingException("Invalid booking status change.");
            }
        }else {
            throw new InvalidBookingException("This booking cannot be changed.");
        }

        booking.setBookingStatus(newStatus);

        if (newStatus == BookingStatus.CANCELLED) {
            RidingSession session = booking.getRidingSession();

            if (session.getSessionStatus() == SessionStatus.FULL) {
                session.setSessionStatus(SessionStatus.AVAILABLE);
            }
        }
        if (newStatus == BookingStatus.CONFIRMED) {
            notificationService.createAndSend(booking.getUser(),
                    "Your booking for " + booking.getRidingSession().getDate() + " was confirmed.",
                    NotificationType.BOOKING_CONFIRMED);
        }
        if (newStatus == BookingStatus.CANCELLED) {
            notificationService.createAndSend(booking.getUser(),
                    "Your booking for " + booking.getRidingSession().getDate() + " was cancelled.",
                    NotificationType.BOOKING_CANCELLED);
        }
    }
}
