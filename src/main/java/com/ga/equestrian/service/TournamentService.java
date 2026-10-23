package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.TournamentRequest;
import com.ga.equestrian.dto.request.UpdateTournamentRequest;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidTournamentException;
import com.ga.equestrian.model.entity.Payment;
import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.model.entity.Tournament;
import com.ga.equestrian.model.enums.PaymentStatus;
import com.ga.equestrian.model.enums.SeatStatus;
import com.ga.equestrian.model.enums.TicketStatus;
import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.repository.SeatRepository;
import com.ga.equestrian.repository.TicketRepository;
import com.ga.equestrian.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    /**
     *
     * @param tournamentRequest
     * @return
     */
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
            seat.setPrice(tournament.getTicketPrice());
            seats.add(seat);
        }

        seatRepository.saveAll(seats);
        return tournament;
    }

    /**
     *
     * @return
     */
    public List<Tournament> getAllTournament(){
        return tournamentRepository.findAll();
    }

    /**
     *
     * @param id
     * @param updateTournamentRequest
     * @return
     */
    @Transactional
    public Tournament updateTournament(Long id, UpdateTournamentRequest updateTournamentRequest){
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(
                () ->  new InformationNotFoundException("Tournament with this id " + id + " not found")
        );

        if(tournament.getTournamentStatus() != TournamentStatus.UPCOMING)
        {
            throw new InvalidTournamentException("Only upcoming tournaments can cancelled.");
        }

        if(updateTournamentRequest.getName() != null){
            tournament.setName(updateTournamentRequest.getName());
        }

        if(updateTournamentRequest.getDate() != null){
            tournament.setDate(updateTournamentRequest.getDate());
        }

        boolean nameOrDateChanged = updateTournamentRequest.getName() != null || updateTournamentRequest.getDate() != null;
        if(nameOrDateChanged && tournamentRepository.existsByNameAndDate(tournament.getName(), tournament.getDate()))
        {
            throw new InformationExistException("A tournament with this name and date already exists.");
        }

        if(updateTournamentRequest.getTime() != null){
            tournament.setTime(updateTournamentRequest.getTime());
        }

        if(updateTournamentRequest.getVenue() != null){
            tournament.setVenue(updateTournamentRequest.getVenue());
        }

        if(updateTournamentRequest.getTicketPrice() != null){
            tournament.setTicketPrice(updateTournamentRequest.getTicketPrice());
            List<Seat> seats = seatRepository.findAllByTournamentIdAndSeatStatus(tournament.getId(), SeatStatus.AVAILABLE);
            seats.forEach(seat -> seat.setPrice(updateTournamentRequest.getTicketPrice()));
            seatRepository.saveAll(seats);
        }

        if(updateTournamentRequest.getNumberOfSeats()!=null){
            long current = seatRepository.countByTournamentId(id);
            if(updateTournamentRequest.getNumberOfSeats() < current){
                throw new InvalidTournamentException("Number of seats cannot be decreased.");
            }
            List<Seat> seats = new ArrayList<>();
            for(int i = ((int) current +1); i<=updateTournamentRequest.getNumberOfSeats(); i++){
                Seat seat = new Seat();
                seat.setTournament(tournament);
                seat.setSeatNumber(i);
                seat.setPrice(tournament.getTicketPrice());
                seats.add(seat);
            }
            seatRepository.saveAll(seats);
        }

        if(updateTournamentRequest.getDescription() != null){
            tournament.setDescription(updateTournamentRequest.getDescription());
        }

        tournamentRepository.save(tournament);
        return tournament;
    }

    /**
     * Cancels an upcoming tournament and its active tickets.
     * Completed payments are refunded and pending payments are marked as failed.
     *
     * @param id the id of the tournament.
     * @return the cancelled tournament.
     */
    public Tournament cancelTournament(Long id){
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(
                () ->  new InformationNotFoundException("Tournament with this id " + id + " not found")
        );

        if(tournament.getTournamentStatus() != TournamentStatus.UPCOMING)
        {
            throw new InvalidTournamentException("Only upcoming tournaments can cancelled.");
        }

        tournament.setTournamentStatus(TournamentStatus.CANCELLED);

        List<Ticket> tickets = ticketRepository.findAllByTournamentIdAndTicketStatusIn(id, List.of(TicketStatus.PENDING, TicketStatus.CONFIRMED));

        for(Ticket ticket: tickets){
            ticket.setTicketStatus(TicketStatus.CANCELLED);

            Payment payment = ticket.getPayment();
            if(payment != null){
                if(payment.getPaymentStatus() == PaymentStatus.COMPLETED){
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                } else if(payment.getPaymentStatus() == PaymentStatus.PENDING){
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                }
            }

        }

        ticketRepository.saveAll(tickets);
        tournamentRepository.save(tournament);
        return tournament;
    }
}
