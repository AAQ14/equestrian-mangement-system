package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.TicketRequest;
import com.ga.equestrian.exception.*;
import com.ga.equestrian.model.entity.*;
import com.ga.equestrian.model.enums.PaymentStatus;
import com.ga.equestrian.model.enums.SeatStatus;
import com.ga.equestrian.model.enums.TicketStatus;
import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final PaymentRepository paymentRepository;

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
                ()->new InformationNotFoundException("User with this email " + email + " not found.")
        );

        return ticketRepository.findAllByUserId(user.getId());
    }


    /**
     * Cancels a ticket for the signed-in user. Only the owner can cancel it,
     * and only while its tournament is upcoming. The seat becomes available again.
     *
     * @param email the email of the signed-in user.
     * @param id the id of the ticket.
     * @return the cancelled ticket.
     * @throws InformationNotFoundException if the ticket does not exist.
     * @throws AccessDeniedException if the ticket belongs to another user.
     * @throws TicketAlreadyCancelledException if the ticket is already cancelled.
     * @throws TicketCancellationNotAllowedException if the tournament is not upcoming.
     */
    @Transactional
    public Ticket cancelTicketByUser(String email, Long id) {

        Ticket ticket = ticketRepository.findById(id).orElseThrow(
                ()->new InformationNotFoundException("Ticket with this id " + id + " not found.")
        );

        if(!ticket.getUser().getEmail().equals(email)){
            throw new AccessDeniedException("You can only cancel your own tickets. ");
        }
        return cancel(ticket);
    }

    /**
     * Cancels any ticket. Used by the admin, so there is no ownership check.
     * The ticket can only be cancelled while its tournament is upcoming, and the seat becomes available again.
     *
     * @param id the id of the ticket.
     * @return the cancelled ticket.
     * @throws InformationNotFoundException if the ticket does not exist.
     * @throws TicketAlreadyCancelledException if the ticket is already cancelled.
     * @throws TicketCancellationNotAllowedException if the tournament is not upcoming.
     */
    @Transactional
    public Ticket cancelTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(
                ()->new InformationNotFoundException("Ticket with this id " + id + " not found.")
        );
        return cancel(ticket);

    }

    /**
     * Applies the cancellation rules shared by the user and admin cancellations.
     * Cancels the ticket and releases its seat.
     *
     * @param ticket the ticket to cancel, already locked by the caller.
     * @return the cancelled ticket.
     * @throws TicketAlreadyCancelledException if the ticket is already cancelled.
     * @throws TicketCancellationNotAllowedException if the tournament is not upcoming.
     */
    private Ticket cancel(Ticket ticket){

        if(ticket.getTicketStatus().equals(TicketStatus.CANCELLED)){
            throw new TicketAlreadyCancelledException("This ticket already cancelled.");
        }

        if(!ticket.getTournament().getTournamentStatus().equals(TournamentStatus.UPCOMING)){
            throw new TicketCancellationNotAllowedException("Tickets can only be cancelled for upcoming tournaments.");
        }

        Seat seat = ticket.getSeat();
        seat.setSeatStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        ticket.setTicketStatus(TicketStatus.CANCELLED);
//        Payment payment = paymentRepository.findById(ticket.getPayment().getId()).orElseThrow(
//                () -> new InformationNotFoundException("payment with this id " + ticket.getPayment().getId() + " not found")
//        );
//        if(payment.getPaymentStatus().equals(PaymentStatus.COMPLETED)){
//            payment.setPaymentStatus(PaymentStatus.REFUNDED);
//        }else if(payment.getPaymentStatus().equals(PaymentStatus.PENDING)){
//            payment.setPaymentStatus(PaymentStatus.FAILED);
//        }
//        paymentRepository.save(payment);
        ticketRepository.save(ticket);

        return ticket;
    }
}
