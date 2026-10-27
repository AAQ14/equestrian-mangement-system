package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseAssignmentRequest;
import com.ga.equestrian.dto.request.HorseAssignmentUpdateRequest;
import com.ga.equestrian.dto.response.HorseAssignmentResponse;
import com.ga.equestrian.mapper.HorseAssignmentMapper;
import com.ga.equestrian.model.entity.Horse;
import com.ga.equestrian.model.entity.HorseAssignment;
import com.ga.equestrian.service.HorseAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HorseAssignmentController {
    private final HorseAssignmentService horseAssignmentService;
    private final HorseAssignmentMapper horseAssignmentMapper;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookings/{bookingId}/horseAssignments")
    public ResponseEntity<HorseAssignmentResponse> creatHorseAssignment(Authentication authentication, @RequestParam Long bookingId, @Valid @RequestBody HorseAssignmentRequest request){
        HorseAssignment horseAssignment = horseAssignmentService.assignHorse(authentication.getName(), bookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(horseAssignmentMapper.toResponse(horseAssignment));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<HorseAssignmentResponse>> getHorseAssignments(Authentication authentication){
        List<HorseAssignmentResponse> horseAssignments = horseAssignmentService.getHorseAssignments(authentication.getName()).
                stream().map(horseAssignmentMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(horseAssignments);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/bookings/{bookingId}/horseAssignments/{horseAssignmentId}")
    public ResponseEntity<HorseAssignmentResponse> updateHorse(Authentication authentication, @RequestParam  Long bookingId, Long horseAssignmentId, @RequestBody HorseAssignmentUpdateRequest horseAssignmentUpdateRequest){
        HorseAssignment horseAssignment = horseAssignmentService.updateHorseAssignment(authentication.getName(), bookingId, horseAssignmentId, horseAssignmentUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(horseAssignmentMapper.toResponse(horseAssignment));
    }

}
