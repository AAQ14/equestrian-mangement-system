package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.TicketRequest;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidTournamentException;
import com.ga.equestrian.exception.SeatAlreadySoldException;
import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.model.entity.Tournament;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.SeatStatus;
import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.repository.SeatRepository;
import com.ga.equestrian.repository.TicketRepository;
import com.ga.equestrian.repository.TournamentRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TournamentRepository tournamentRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;


    @Transactional
    public Ticket buyTicket(String email, TicketRequest ticketRequest) {
        Long seatId = ticketRequest.getSeatId();
        Seat seat = seatRepository.findById(seatId).orElseThrow(
                ()->new InformationNotFoundException("Seat with this id " + seatId + " not found.")
        );

        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email " + email + " not found.")
        );

       Tournament tournament = seat.getTournament();
       if(tournament.getTournamentStatus()!= TournamentStatus.UPCOMING && tournament.getTournamentStatus()!=TournamentStatus.ONGOING) {
           throw new InvalidTournamentException("Tickets can only be bought for upcoming or ongoing tournaments.");
       }
       if(seat.getSeatStatus() != SeatStatus.AVAILABLE){
           throw new SeatAlreadySoldException("Seat " + seat.getSeatNumber() + " is already sold.");
       }

       seat.setSeatStatus(SeatStatus.SOLD);
       seatRepository.save(seat);

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setTournament(tournament);
        ticket.setSeat(seat);
        ticket.setPrice(seat.getPrice());
        ticketRepository.save(ticket);
        return ticket;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getAllTicketsByUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email " + email + " not found")
        );

        return ticketRepository.findALLByUserId(user.getId());
    }
}
