package com.ga.equestrian.bookingservice.service;

import com.ga.equestrian.bookingservice.client.UserClient;
import com.ga.equestrian.bookingservice.dto.request.*;
import com.ga.equestrian.bookingservice.dto.response.*;
import com.ga.equestrian.bookingservice.exception.*;
import com.ga.equestrian.bookingservice.mapper.*;
import com.ga.equestrian.bookingservice.model.enums.*;
import com.ga.equestrian.bookingservice.model.entity.*;
import com.ga.equestrian.bookingservice.repository.*;
import com.ga.equestrian.bookingservice.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {
    private static final List<BookingStatus> SEAT_HOLDING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RidingSessionRepository ridingSessionRepository;
    private final BookingMapper bookingMapper;
    private final UserClient userClient;

    @Transactional
    public BookingResponse createBooking(AuthUser authUser, BookingRequest request){

        Long userBookedId = authUser.userId();


        if(request.getUserId()!=null) {
            if (authUser.role().equals("CLIENT")) {
                throw new AccessDeniedException("Clients can only book for themselves.");
            }
            UserSummary bookedUser = userClient.getUser(request.getUserId());

            if (bookedUser.userStatus() != UserStatus.ACTIVE) {
                throw new InvalidBookingException("Bookings can only be made for active users.");
            }

            userBookedId = bookedUser.id();
         }
            RidingSession session = ridingSessionRepository.findById(request.getRidingSessionId()).orElseThrow(
                    () -> new InformationNotFoundException("Riding session with id " + request.getRidingSessionId() + " not found.")
            );
            if(authUser.role().equals(Role.INSTRUCTOR.name()) && request.getUserId() !=null && !session.getInstructor().getUserId().equals(authUser.userId())){
                throw new AccessDeniedException("Instructors can only book clients into their own sessions. ");
            }
            if (session.getSessionStatus() != SessionStatus.AVAILABLE) {
                throw new InvalidBookingException("This session is not available for booking.");
            }

            LocalDateTime sessionStart = LocalDateTime.of(session.getDate(), session.getStartTime());
            if (sessionStart.isBefore(LocalDateTime.now())) {
                throw new InvalidBookingException("This session has already started or finished.");
            }

            if(bookingRepository.existsByUserIdAndRidingSessionIdAndBookingStatusNot(userBookedId, session.getId(), BookingStatus.CANCELLED)){
                throw new InformationExistException("This user already has a booking for this session.");
            }

             if(bookingRepository.existsByUserIdAndRidingSessionDateAndRidingSessionStartTimeLessThanAndRidingSessionEndTimeGreaterThanAndBookingStatusIn(
                userBookedId, session.getDate(), session.getEndTime(), session.getStartTime(), SEAT_HOLDING_STATUSES)){
                 throw new InformationExistException("This user already has a booking that overlaps this session.");
             }

            long takenSpots = bookingRepository.countByRidingSessionIdAndBookingStatusIn(session.getId(), SEAT_HOLDING_STATUSES);
            if(takenSpots>= session.getCapacity()){
                throw new InformationExistException("Riding session with id " + session.getId() + " is fully booked.");
            }

            Booking booking = new Booking();
            booking.setUserId(userBookedId);
            booking.setRidingSession(session);
            Booking savedBooking = bookingRepository.save(booking);
            if (takenSpots + 1 >= session.getCapacity()) {
                session.setSessionStatus(SessionStatus.FULL);
            }
        log.info("User {} created booking {} for session {} (booked user {})",
               authUser.userId(), savedBooking.getId(), session.getId(), userBookedId);

            //ToDo: publish BOOKING_CREATED event
            return bookingMapper.toResponse(savedBooking);

    }

    /**
     * Returns bookings as response DTOs, optionally filtered by status.
     * Clients get only their own bookings; instructors and admins get all of them.
     *
     * @param authUser the signed-in user (id, email, role), taken from the JWT.
     * @param status the booking status to filter by, or null for all statuses.
     * @return the list of bookings, empty if there are none.
     * @throws InformationNotFoundException if the signed-in user does not exist.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(AuthUser authUser, BookingStatus status) {

       List<Booking> bookings = new ArrayList<>();

        if(authUser.role().equals(Role.CLIENT.name())){
            if(status != null){
                bookings = bookingRepository.findByUserIdAndBookingStatus(authUser.userId(), status);
            } else{
                bookings = bookingRepository.findByUserId(authUser.userId());
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



//    /**
//     * Finds a user by the email stored in the JWT.
//     *
//     * @param email the user's email.
//     * @return the user.
//     * @throws InformationNotFoundException if no user has this email.
//     */
//    private User findUserByEmail(String email) {
//        return userRepository.findByEmail(email).orElseThrow(
//                () -> new InformationNotFoundException("User with email " + email + " not found."));
//    }


    /**
     * Returns one booking by id.
     * Clients can only view their own bookings.
     *
     * @param id the booking id.
     * @return the booking as a response DTO.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client requests another user's booking.
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, AuthUser authUser){
        return bookingMapper.toResponse(findAccessibleBooking(id, authUser));
    }

    /**
     * Changes the status of a booking, following the allowed transitions.
     * Clients can only cancel their own bookings; instructors
     * can apply any allowed change.
     *
     * @param id the booking id.
     * @param request  the new status.
     * @return the updated booking as a response DTO.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client tries to change another user's booking or to set a status other than CANCELLED.
     * @throws InvalidBookingException if the status change is not allowed.
     */
    @Transactional
    public BookingResponse updateBooking(Long id, AuthUser authUser, UpdateBookingRequest request){
        Booking booking = findAccessibleBooking(id, authUser);
        if(authUser.role().equals( Role.CLIENT.name()) && request.getBookingStatus() != BookingStatus.CANCELLED){
            throw new AccessDeniedException("Clients can only cancel their bookings.");
        }
        changeStatus(booking, request.getBookingStatus());
        log.info("User {} changed booking {} to {}", authUser.userId(), booking.getId(), booking.getBookingStatus());
        return bookingMapper.toResponse(booking);
    }

    /**
     * Cancels a booking by id. The booking is kept in the database with the CANCELLED status,
     * and its spot in the session is released.
     *
     * @param id the booking id.
     * @return the cancelled booking.
     * @throws InformationNotFoundException if the booking or the signed-in user does not exist.
     * @throws AccessDeniedException if a client cancels another user's booking.
     * @throws InvalidBookingException if the booking is already cancelled or completed.
     */
    @Transactional
    public BookingResponse deleteBooking(Long id, AuthUser authUser){
        Booking booking = findAccessibleBooking(id, authUser);
        changeStatus(booking, BookingStatus.CANCELLED);
        log.info("User {} cancelled booking {}", authUser.userId(), booking.getId());
        return bookingMapper.toResponse(booking);
    }

    /**
     * Finds a booking and checks that the signed-in user is allowed to access it.
     *
     * @param id the booking id.
     * @param authUser the user making the request.
     * @return the booking.
     * @throws InformationNotFoundException if no booking has this id.
     * @throws AccessDeniedException if a client asks for another user's booking.
     */
    private Booking findAccessibleBooking(Long id,AuthUser authUser) {
        Booking booking = bookingRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Booking with id " + id + " not found."));
        boolean isBookingOwner = booking.getUserId().equals(authUser.userId());
        boolean isSessionInstructor = booking.getRidingSession().getInstructor().getUserId().equals(authUser.userId());
        if (!isBookingOwner && !isSessionInstructor) {
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
            //todo: send notification if the booking is confirmed
        }
        if (newStatus == BookingStatus.CANCELLED) {
            //todo: send notification if the booking is cancelled
        }
    }
}
