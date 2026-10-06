package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.HorseRequest;
import com.ga.equestrian.dto.request.UpdateHorseRequest;
import com.ga.equestrian.dto.response.HorseResponse;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HorseController {

    private final HorseService horseService;

    /**
     *  Creates a new horse.
     *
     * @param horseRequest the data of the horse to create.
     * @return a success message when the horse is created.
     */
    @PostMapping("/horses/create")
    public ResponseEntity<String> addHorse(@Valid @ModelAttribute HorseRequest horseRequest){
        horseService.createHorse(horseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Horse created successfully");
    }

    /**
     * Updates an existing horse by its ID.
     *
     * @param id the ID of the horse to update.
     * @param updateHorseRequest the updated horse data.
     * @return  a success message when the horse is updated.
     */
    @PutMapping("/horses/update/{id}")
    public ResponseEntity<String> updateHorse(@PathVariable Long id,@Valid @ModelAttribute UpdateHorseRequest updateHorseRequest){
        horseService.updateHorse(id, updateHorseRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Horse updated successfully");
    }

    /**
     *  Deletes a horse by its ID.
     *
     * @param id the ID of the horse to delete.
     * @return a success message when the horse is deleted.
     */
    @DeleteMapping("/horses/delete/{id}")
    public ResponseEntity<String> deleteHorse(@PathVariable Long id){
        horseService.deleteHorse(id);
        return ResponseEntity.status(HttpStatus.OK).body("Horse deleted successfully");
    }

    /**
     * Get all horses stored in the database.
     *
     * @return a list of all horses responses.
     */
    @GetMapping("/horses/all")
    public ResponseEntity<List<HorseResponse>> getAllHorses() {
        List<HorseResponse> horses = horseService.getAllHorses();
        return ResponseEntity.ok(horses);
    }

    /**
     * Gets a horse by its ID.
     * @param id the ID of the horse to retrieve.
     * @return the horse response.
     */
    @GetMapping("/horses/{id}")
    public ResponseEntity<HorseResponse> getHorse(@PathVariable Long id){
        HorseResponse horse = horseService.getHorse(id);
        return ResponseEntity.ok(horse);
    }
}
