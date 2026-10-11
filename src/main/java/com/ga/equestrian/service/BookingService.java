package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.BookingRequest;
import com.ga.equestrian.dto.response.BookingResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidBookingException;
import com.ga.equestrian.mapper.BookingMapper;
import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.BookingStatus;
import com.ga.equestrian.model.enums.Role;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.UserStatus;
import com.ga.equestrian.repository.BookingRepository;
import com.ga.equestrian.repository.RidingSessionRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return bookingMapper.toResponse(savedBooking);

    }

    /**
     * Returns bookings as response DTOs.
     * Clients get only their own bookings; instructors and admins get all of them.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @return the list of bookings, empty if there are none.
     * @throws InformationNotFoundException if the signed-in user does not exist.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(String email) {
        User signedInUser = findUserByEmail(email);
        List<Booking> bookings = signedInUser.getRole() == Role.CLIENT
                ? bookingRepository.findByUserId(signedInUser.getId())
                : bookingRepository.findAll();
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
}
