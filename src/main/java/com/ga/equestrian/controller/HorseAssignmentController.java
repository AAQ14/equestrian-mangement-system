package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseAssignmentRequest;
import com.ga.equestrian.dto.request.HorseAssignmentUpdateRequest;
import com.ga.equestrian.dto.response.HorseAssignmentResponse;
import com.ga.equestrian.mapper.HorseAssignmentMapper;
import com.ga.equestrian.model.entity.HorseAssignment;
import com.ga.equestrian.service.HorseAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Handles the endpoints for assigning a horse to a booking, changing it, and viewing the assignments.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HorseAssignmentController {
    private final HorseAssignmentService horseAssignmentService;
    private final HorseAssignmentMapper horseAssignmentMapper;

    /**
     * Assigns a horse to a booking.
     * Returns 201 on success, 400 if the horse id is missing, 403 if the user is not the booking's owner
     * or the session's instructor, 404 if the booking or horse is not found, 409 if the booking already
     * has a horse or the horse is busy at that time, and 422 if the booking or session is not active
     * or the horse is not available.
     *
     * @param authentication the signed-in user.
     * @param bookingId the id of the booking.
     * @param request the id of the horse to assign.
     * @return the created horse assignment.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookings/{bookingId}/horseAssignments")
    public ResponseEntity<HorseAssignmentResponse> createHorseAssignment(Authentication authentication, @PathVariable Long bookingId, @Valid @RequestBody HorseAssignmentRequest request){
        HorseAssignment horseAssignment = horseAssignmentService.assignHorse(authentication.getName(), bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(horseAssignmentMapper.toResponse(horseAssignment));
    }

    /**
     * Gets the horse assignments of the signed-in user's bookings.
     * Returns 200 on success, with an empty list if there are none.
     *
     * @param authentication the signed-in user.
     * @return a list of the user's horse assignments.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/horseAssignments/my")
    public ResponseEntity<List<HorseAssignmentResponse>> getHorseAssignments(Authentication authentication){
        List<HorseAssignmentResponse> horseAssignments = horseAssignmentService.getHorseAssignments(authentication.getName()).
                stream().map(horseAssignmentMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(horseAssignments);
    }

    /**
     * Changes the horse assigned to a booking.
     * Returns 200 on success, 400 if the horse id is missing, 403 if the booking belongs to another user,
     * 404 if the booking has no assignment or the horse is not found, 409 if it is already the booking's
     * horse or the new horse is busy at that time, and 422 if the booking or session is not active
     * or the horse is not available.
     *
     * @param authentication the signed-in user.
     * @param bookingId the id of the booking.
     * @param horseAssignmentUpdateRequest the id of the new horse.
     * @return the updated horse assignment.
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/bookings/{bookingId}/horseAssignments/")
    public ResponseEntity<HorseAssignmentResponse> updateHorse(Authentication authentication, @PathVariable  Long bookingId, @Valid @RequestBody HorseAssignmentUpdateRequest horseAssignmentUpdateRequest){
        HorseAssignment horseAssignment = horseAssignmentService.updateHorseAssignment(authentication.getName(), bookingId, horseAssignmentUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(horseAssignmentMapper.toResponse(horseAssignment));
    }

    /**
     * Gets the horse assigned to a booking.
     * Returns 200 on success, 403 if the user is not the booking's owner or the session's instructor,
     * and 404 if the booking has no horse assigned.
     *
     * @param authentication the signed-in user.
     * @param bookingId the id of the booking.
     * @return the horse assignment of the booking.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookings/{bookingId}/horseAssignments")
    public ResponseEntity<HorseAssignmentResponse> getHorseAssignment(Authentication authentication, @PathVariable Long bookingId){
        HorseAssignment horseAssignment = horseAssignmentService.getHorseAssignmentByBooking(authentication.getName(), bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(horseAssignmentMapper.toResponse(horseAssignment));
    }

    /**
     * Removes the horse from a booking.
     * Returns 204 on success, 403 if the user is not the booking's owner or the session's instructor,
     * 404 if the booking has no horse assigned, and 422 if the session is already completed.
     *
     * @param authentication the signed-in user.
     * @param bookingId the id of the booking.
     * @return an empty response.
     */

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/bookings/{bookingId}/horseAssignments")
    public ResponseEntity<?> cancelHorseAssignment(Authentication authentication, @PathVariable Long bookingId) {
        horseAssignmentService.cancelHorseAssignment(authentication.getName(), bookingId);
        return ResponseEntity.status(HttpStatus.OK).body("horse assignment cancelled");
    }
}
