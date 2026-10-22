package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.TournamentRequest;
import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.model.entity.Tournament;
import com.ga.equestrian.repository.SeatRepository;
import com.ga.equestrian.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final SeatRepository seatRepository;

    public Tournament add(TournamentRequest tournamentRequest){
        Tournament tournament = new Tournament();
        tournament.setName(tournamentRequest.getName());
        tournament.setTournamentType(tournamentRequest.getTournamentType());
        tournament.setDate(tournamentRequest.getDate());
        tournament.setTime(tournamentRequest.getTime());
        tournament.setVenue(tournamentRequest.getVenue());
        tournament.setTicketPrice(tournamentRequest.getTicketPrice());
        tournament.setDescription(tournamentRequest.getDescription());

        tournamentRepository.save(tournament);

        List<Seat> seats = new ArrayList<>();
        for(int i = 1; i<=tournamentRequest.getNumberOfSeats(); i++){
            Seat seat = new Seat();
            seat.setTournament(tournament);
            seat.setSeatNumber(i);
            seats.add(seat);
        }

        seatRepository.saveAll(seats);
        return tournament;
    }
}
