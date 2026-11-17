package com.ga.equestrian.bookingservice.controller;

import com.ga.equestrian.bookingservice.dto.request.InstructorRequest;
import com.ga.equestrian.bookingservice.dto.response.InstructorResponse;
import com.ga.equestrian.bookingservice.security.AuthUser;
import com.ga.equestrian.bookingservice.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InstructorController {

    private final InstructorService instructorService;

    /**
     * Creates an instructor profile for an existing user, and gives the user the INSTRUCTOR role.
     * Returns 201 on success, 400 if the data is invalid, 403 if the caller is not an admin,
     * 404 if the user does not exist, 409 if the user is already an instructor,
     * and 422 if the user is not active.
     *
     * @param authUser the signed-in admin, taken from the JWT.
     * @param request the user id and the instructor details.
     * @return the created instructor.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/instructors/create")
    public ResponseEntity<InstructorResponse> addInstructor(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody InstructorRequest request){
        InstructorResponse instructorResponse = instructorService.addInstructor(authUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(instructorResponse);
    }

    /**
     * Returns all instructors.
     * Returns 200 on success and 404 if there are no instructors.
     *
     * @return the list of instructors.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructors")
    public ResponseEntity<List<InstructorResponse>> getAllInstructors(){
        List<InstructorResponse> allInstructors = instructorService.getAllInstructors();
        return ResponseEntity.status(HttpStatus.OK).body(allInstructors);
    }

    /**
     * Returns one instructor by id.
     * Returns 200 on success and 404 if the instructor does not exist.
     *
     * @param id the instructor id.
     * @return the instructor.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> getInstructorById(@PathVariable Long id){
        InstructorResponse instructor = instructorService.getInstructorById(id);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }

    /**
     * Updates an instructor's experience, skill level and specialization.
     * Returns 200 on success, 400 if the data is invalid, 403 if the caller is not an admin,
     * and 404 if the instructor does not exist.
     *
     * @param authUser the signed-in admin, taken from the JWT.
     * @param id the instructor id.
     * @param instructorRequest the new instructor details.
     * @return the updated instructor.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> updateInstructor(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody InstructorRequest instructorRequest, @PathVariable Long id){
        InstructorResponse instructor = instructorService.updateInstructor(id, instructorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }

    /**
     * Deletes an instructor profile. The user account is kept and goes back to the CLIENT role.
     * Returns 200 on success, 403 if the caller is not an admin, and 404 if the instructor does not exist.
     *
     * @param authUser the signed-in admin, taken from the JWT.
     * @param id the instructor id.
     * @return the deleted instructor.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> deleteInstructor(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id){
        InstructorResponse instructor = instructorService.deleteInstructor(authUser, id);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }

}
