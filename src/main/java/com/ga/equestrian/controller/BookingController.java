package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.BookingRequest;
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
@RequestMapping("/api")
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
    @PostMapping("/bookings")
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
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getAllBookings(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(bookings);
    }
}
