package com.ga.equestrian.bookingservice.controller;

import com.ga.equestrian.bookingservice.dto.request.HorseRequest;
import com.ga.equestrian.bookingservice.dto.request.UpdateHorseRequest;
import com.ga.equestrian.bookingservice.dto.response.HorseResponse;
import com.ga.equestrian.bookingservice.service.HorseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('INSTRUCTOR')")
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
    @PreAuthorize("hasRole('INSTRUCTOR')")
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
    @PreAuthorize("hasRole('INSTRUCTOR')")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/horses/{id}")
    public ResponseEntity<HorseResponse> getHorse(@PathVariable Long id){
        HorseResponse horse = horseService.getHorse(id);
        return ResponseEntity.ok(horse);
    }
}
