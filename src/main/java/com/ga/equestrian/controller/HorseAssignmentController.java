package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseAssignmentRequest;
import com.ga.equestrian.dto.response.HorseAssignmentResponse;
import com.ga.equestrian.model.entity.HorseAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HorseAssignmentController {
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/assignHorse")
//    public ResponseEntity<HorseAssignmentResponse> creatHorseAssignment(Authentication authentication, @RequestBody HorseAssignmentRequest request){
//        Hos
//    }
}
