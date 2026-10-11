package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.RidingSessionRequest;
import com.ga.equestrian.dto.request.UpdateRidingSessionRequest;
import com.ga.equestrian.dto.response.RidingSessionResponse;
import com.ga.equestrian.service.RidingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<RidingSessionResponse> addSession(@Valid @RequestBody RidingSessionRequest request) {
        RidingSessionResponse response = ridingSessionService.addSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    public ResponseEntity<List<RidingSessionResponse>> getAllSessions()
    {
        List<RidingSessionResponse> allRidingSessions = ridingSessionService.getAllSessions();
        return ResponseEntity.status(HttpStatus.OK).body(allRidingSessions);
    }


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
     * Deletes a riding session by id.
     * Returns 200 on success, 403 if the user is not an instructor,
     * and 404 if the session does not exist.
     *
     * @param id the session id.
     * @return the deleted session.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<RidingSessionResponse> deleteSession(@PathVariable Long id) {
        RidingSessionResponse response = ridingSessionService.deleteSession(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
