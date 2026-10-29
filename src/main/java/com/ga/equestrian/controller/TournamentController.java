package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.TournamentRequest;
import com.ga.equestrian.dto.request.UpdateTournamentRequest;
import com.ga.equestrian.dto.response.TournamentResponse;
import com.ga.equestrian.mapper.TournamentMapper;
import com.ga.equestrian.model.entity.Tournament;
import com.ga.equestrian.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentMapper tournamentMapper;

    /**
     *
     * @param tournamentRequest
     * @return
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TournamentResponse> addTournament(@Valid @RequestBody TournamentRequest tournamentRequest){
        Tournament tournament = tournamentService.add(tournamentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(tournamentMapper.toResponse(tournament));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<?> getAllTournament(){
        List<Tournament> tournaments = tournamentService.getAllTournament();
//        if(tournaments.isEmpty()){
//            return ResponseEntity.status(HttpStatus.OK).body("There are no tournaments.");
//        }
        List<TournamentResponse> responses = tournaments.stream().map(tournamentMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<TournamentResponse> updateTournament(@PathVariable Long id, @Valid @RequestBody UpdateTournamentRequest updateTournamentRequest){
            Tournament tournament = tournamentService.updateTournament(id, updateTournamentRequest);
            return ResponseEntity.status(HttpStatus.OK).body(tournamentMapper.toResponse(tournament));
    }

    /**
     * Cancels a tournament and its active tickets.
     *
     * @param id the id of the tournament.
     * @return the cancelled tournament.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TournamentResponse> cancelTournament(Authentication authentication,  @PathVariable Long id){
        Tournament tournament = tournamentService.cancelTournament(authentication.getName(), id);
        return ResponseEntity.status(HttpStatus.OK).body(tournamentMapper.toResponse(tournament));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id){
        Tournament tournament = tournamentService.getTournamentById(id);
        return ResponseEntity.status(HttpStatus.OK).body(tournamentMapper.toResponse(tournament));
    }
}
