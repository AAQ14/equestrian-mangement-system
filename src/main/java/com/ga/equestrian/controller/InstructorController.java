package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.InstructorRequest;
import com.ga.equestrian.dto.response.InstructorResponse;
import com.ga.equestrian.model.entity.Instructor;
import com.ga.equestrian.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InstructorController {

    private final InstructorService instructorService;

    @PostMapping("/instructors/create")
    public ResponseEntity<InstructorResponse> addInstructor(@Valid @RequestBody InstructorRequest request){
        InstructorResponse instructorResponse = instructorService.addInstructor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(instructorResponse);
    }
}
