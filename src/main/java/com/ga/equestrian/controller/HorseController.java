package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseRequest;
import com.ga.equestrian.dto.request.UpdateHorseRequest;
import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.repository.HorseRepository;
import com.ga.equestrian.service.HorseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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

    @PutMapping("/horses/update/{id}")
    public ResponseEntity<String> updateHorse(@PathVariable Long id,@Valid @ModelAttribute UpdateHorseRequest updateHorseRequest){
        horseService.updateHorse(id, updateHorseRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Horse updated successfully");
    }

    @DeleteMapping("/horses/delete/{id}")
    public ResponseEntity<String> deleteHorse(@PathVariable Long id){
        horseService.deleteHorse(id);
        return ResponseEntity.status(HttpStatus.OK).body("Horse deleted successfully");
    }
}
