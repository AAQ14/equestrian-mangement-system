package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.BookingRequest;
import com.ga.equestrian.dto.request.UpdateBookingRequest;
import com.ga.equestrian.dto.response.BookingResponse;
import com.ga.equestrian.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    /**
     * Creates a booking for a riding session.
     * Returns 201 on success, 400 if data is invalid, 403 if a client books for another user,
     * 404 if the user or session does not exist, 409 if the session is full or already booked,
     * and 422 if the session is unavailable or in the past.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @param request the session id and the optional user id.
     * @return the created booking.
     */
    @PostMapping()
    public ResponseEntity<BookingResponse> createBooking(Authentication authentication, @Valid @RequestBody BookingRequest request){
        BookingResponse response = bookingService.createBooking(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns bookings. Clients get their own; instructors and admins get all.
     * Returns 200 on success.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the list of bookings.
     */
    @GetMapping()
    public ResponseEntity<List<BookingResponse>> getAllBookings(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getAllBookings(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(bookings);
    }

    /**
     * Returns one booking by id.
     * Returns 200 on success, 403 if a client requests another user's booking,
     * and 404 if the booking does not exist.
     *
     * @param id the booking id.
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the booking.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id, Authentication authentication) {
        BookingResponse booking = bookingService.getBookingById(id, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }


    /**
     * Changes the status of a booking.
     * Returns 200 on success, 400 if data is invalid, 403 if the user is not allowed,
     * 404 if the booking does not exist, and 422 if the status change is not allowed.
     *
     * @param id the booking id.
     * @param request the new status.
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the updated booking.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id, @Valid @RequestBody UpdateBookingRequest request, Authentication authentication)
    {
        BookingResponse booking = bookingService.updateBooking(id, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }


    /**
     * Cancels a booking by id. The booking is kept with the CANCELLED status.
     * Returns 200 on success, 403 if the user is not allowed,
     * 404 if the booking does not exist, and 422 if it is already cancelled or completed.
     *
     * @param id the booking id.
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the cancelled booking.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> deleteBooking(@PathVariable Long id, Authentication authentication){
        BookingResponse booking = bookingService.deleteBooking(id, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }

}
