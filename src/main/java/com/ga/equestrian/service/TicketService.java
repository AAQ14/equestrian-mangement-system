package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.TicketRequest;
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

import java.util.List;

/**
 * Service that handles buying and viewing tickets.
 */
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TournamentRepository tournamentRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    /**
     * Buys a ticket for a seat of a tournament. The price comes from the seat,
     * and the seat becomes SOLD.
     *
     * @param email the email of the signed-in user.
     * @param tournamentId the id of the tournament.
     * @param ticketRequest the request holding the id of the seat.
     * @return the bought ticket.
     * @throws InformationNotFoundException if the seat, user or tournament does not exist.
     * @throws InvalidTournamentException if the seat is not in this tournament, or the tournament is not upcoming or ongoing.
     * @throws SeatAlreadySoldException if the seat is already sold.
     */
    @Transactional
    public Ticket buyTicket(String email, Long tournamentId, TicketRequest ticketRequest) {
        Long seatId = ticketRequest.getSeatId();
        Seat seat = seatRepository.findById(seatId).orElseThrow(
                ()->new InformationNotFoundException("Seat with this id " + seatId + " not found.")
        );

        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email " + email + " not found.")
        );

       Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(
               ()-> new InformationNotFoundException("Tournament with this id " + tournamentId + " not found" )
       );

       if(!seat.getTournament().getId().equals(tournamentId)){
           throw new InvalidTournamentException("Seat " + seat.getSeatNumber() + " does not belong to this tournament. ");
       }

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

    /**
     * Gets all the tickets. Used by the admin.
     *
     * @return a list of all the tickets.
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Gets all the tickets of the signed-in user.
     *
     * @param email the email of the signed-in user.
     * @return a list of the user's tickets.
     */
    public List<Ticket> getAllTicketsByUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email " + email + " not found")
        );

        return ticketRepository.findAllByUserId(user.getId());
    }
}
