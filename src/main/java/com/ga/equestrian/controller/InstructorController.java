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

import java.util.List;

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

    @GetMapping("/instructors")
    public ResponseEntity<List<InstructorResponse>> getAllInstructors(){
        List<InstructorResponse> allInstructors = instructorService.getAllInstructors();
        return ResponseEntity.status(HttpStatus.OK).body(allInstructors);
    }

    @GetMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> getInstructorById(@PathVariable Long id){
        InstructorResponse instructor = instructorService.getInstructorById(id);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }

    @PutMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> updateInstructor(@Valid @RequestBody InstructorRequest instructorRequest, @PathVariable Long id){
        InstructorResponse instructor = instructorService.updateInstructor(id, instructorRequest);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }

    @DeleteMapping("/instructors/{id}")
    public ResponseEntity<InstructorResponse> deleteInstructor(@PathVariable Long id){
        InstructorResponse instructor = instructorService.deleteInstructor(id);
        return ResponseEntity.status(HttpStatus.OK).body(instructor);
    }
}
