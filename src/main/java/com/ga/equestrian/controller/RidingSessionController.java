package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.RidingSessionRequest;
import com.ga.equestrian.dto.request.UpdateRidingSessionRequest;
import com.ga.equestrian.dto.response.RidingSessionResponse;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.service.RidingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Paths related to riding sessions
 * Everyone who is signed in can view sessions; only instructors can create, edit or delete them.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class RidingSessionController {

    private final RidingSessionService ridingSessionService;

    /**
     * Creates a riding session.
     *  Returns 201 on success, 400 if data is invalid, 403 if the user is not an instructor,
     *  or not an admin and 404 if the instructor does not exist.
     *
     * @param request the session details sent by the instructor or the admin.
     * @return the created session.
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<RidingSessionResponse> addSession(@Valid @RequestBody RidingSessionRequest request) {
        RidingSessionResponse response = ridingSessionService.addSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Returns riding sessions, optionally filtered by status, skill level or date.
     * Only one filter is applied at a time, in this order: status, skill level, date.
     * Returns 200 on success, 400 if a filter value is invalid, and 404 if no sessions match.
     *
     * @param sessionStatus the session status to filter by, or null.
     * @param skillLevel the skill level to filter by, or null.
     * @param date the session date to filter by (yyyy-MM-dd), or null.
     * @return the list of sessions.
     */
    @GetMapping
    public ResponseEntity<List<RidingSessionResponse>> getAllSessions(@RequestParam(required = false)SessionStatus sessionStatus, @RequestParam(required = false)SkillLevel skillLevel, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date)
    {
        List<RidingSessionResponse> allRidingSessions = ridingSessionService.getAllSessions(sessionStatus, skillLevel, date);
        return ResponseEntity.status(HttpStatus.OK).body(allRidingSessions);
    }


    /**
     * Get a riding session by id.
     * Returns 200 on success and 404 if there are no sessions.
     *
     * @param id the session id.
     * @return the session.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RidingSessionResponse> getSessionById(@PathVariable Long id) {
        RidingSessionResponse response = ridingSessionService.getSessionById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a riding session by id.
     * Returns 200 on success, 400 if data is invalid, 403 if the user is not an instructor,
     * and 404 if the session or the instructor does not exist.
     *
     * @param id the session id.
     * @param request the new session details.
     * @return the updated session.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<RidingSessionResponse> updateSession(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateRidingSessionRequest request) {
        RidingSessionResponse response = ridingSessionService.updateSession(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     *
     * Cancels a riding session and its active bookings. Instructor only.
     * Returns 200 on success, 403 if the user is not the session's instructor,
     * 404 if the session does not exist, and 422 if the session is already cancelled or completed.
     *
     * @param authentication the signed-in instructor.
     * @param id the session id.
     * @return the cancelled session.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<RidingSessionResponse> deleteSession(Authentication authentication,  @PathVariable Long id) {
        RidingSessionResponse response = ridingSessionService.cancelSession(authentication.getName(), id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
