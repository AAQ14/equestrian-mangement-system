package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseRequest;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.repository.HorseRepository;
import com.ga.equestrian.service.HorseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HorseController {

    private final HorseService horseService;

    @PostMapping("/horses/create")
    public ResponseEntity<String> addHorse(@Valid @ModelAttribute HorseRequest horseRequest){
        horseService.createHorse(horseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Horse created successfully");
    }
}
