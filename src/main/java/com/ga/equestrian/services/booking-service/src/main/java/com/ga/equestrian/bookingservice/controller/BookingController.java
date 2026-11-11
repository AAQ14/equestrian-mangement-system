package com.ga.equestrian.bookingservice.controller;

import com.ga.equestrian.bookingservice.dto.request.BookingRequest;
import com.ga.equestrian.bookingservice.dto.request.UpdateBookingRequest;
import com.ga.equestrian.bookingservice.dto.response.BookingResponse;
import com.ga.equestrian.bookingservice.model.enums.BookingStatus;
import com.ga.equestrian.bookingservice.security.AuthUser;
import com.ga.equestrian.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * @param request the session id and the optional user id.
     * @return the created booking.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody BookingRequest request){
        BookingResponse response = bookingService.createBooking(authUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns bookings. Clients get their own; instructors and admins get all.
     * Returns 200 on success.
     *
     * @param status the booking status to filter by, or null for all.
     * @return the list of bookings.
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(@AuthenticationPrincipal AuthUser authUser, @RequestParam(required = false)BookingStatus status) {
        List<BookingResponse> bookings = bookingService.getAllBookings(authUser, status);
        return ResponseEntity.status(HttpStatus.OK).body(bookings);
    }

    /**
     * Returns one booking by id.
     * Returns 200 on success, 403 if a client requests another user's booking,
     * and 404 if the booking does not exist.
     *
     * @param id the booking id.
     * @return the booking.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
        BookingResponse booking = bookingService.getBookingById(id, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }


    /**
     * Changes the status of a booking.
     * Returns 200 on success, 400 if data is invalid, 403 if the user is not allowed,
     * 404 if the booking does not exist, and 422 if the status change is not allowed.
     *
     * @param id the booking id.
     * @param request the new status.
     * @return the updated booking.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id, @Valid @RequestBody UpdateBookingRequest request,@AuthenticationPrincipal AuthUser authUser)
    {
        BookingResponse booking = bookingService.updateBooking(id, authUser, request);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }


    /**
     * Cancels a booking by id. The booking is kept with the CANCELLED status.
     * Returns 200 on success, 403 if the user is not allowed,
     * 404 if the booking does not exist, and 422 if it is already cancelled or completed.
     *
     * @param id the booking id.
     * @return the cancelled booking.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> deleteBooking(@PathVariable Long id,@AuthenticationPrincipal AuthUser authUser){
        BookingResponse booking = bookingService.deleteBooking(id, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }

}
