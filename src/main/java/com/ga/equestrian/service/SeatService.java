package com.ga.equestrian.service;

import com.ga.equestrian.dto.response.SeatResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.repository.SeatRepository;
import com.ga.equestrian.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {
    private final SeatRepository seatRepository;
    private final TournamentRepository tournamentRepository;

    /**
     * Get all the seats.
     *
     * @return a list of all the seats as response.
     */
    public List<Seat> getAllSeats(){
        return seatRepository.findAll();
    }


    public List<Seat> getSeatsByTournament(Long tournamentId){
        if(!tournamentRepository.existsById(tournamentId)){
            throw new InformationNotFoundException("Tournament with this id " + tournamentId + " not found.");
        }
        return seatRepository.findAllByTournamentId(tournamentId);
    }
}
