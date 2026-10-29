package com.ga.equestrian.controller;

import com.ga.equestrian.dto.response.SeatResponse;
import com.ga.equestrian.mapper.SeatMapper;
import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.service.SeatService;
import jdk.jshell.Snippet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;
    private final SeatMapper seatMapper;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<SeatResponse>> getAllSeats(){
        List<SeatResponse> seats = seatService.getAllSeats().stream().map(seatMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(seats);
    }

    /**
     *
     * @param tournamentId
     * @return
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{tournamentId}")
    public ResponseEntity<List<SeatResponse>> getAllSeatsByTournament(@PathVariable Long tournamentId){
        List<SeatResponse> seats = seatService.getSeatsByTournament(tournamentId).stream().map(seatMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(seats);
    }
}
