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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service that handles buying and viewing tickets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TournamentRepository tournamentRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
    private final TransactionTemplate transactionTemplate;

    public Ticket buyTicket(String email, Long tournamentId, TicketRequest ticketRequest){
        Long seatId = ticketRequest.getSeatId();
        //one lock per seat: get this seat's lock from the map, or create it the first time someone buys this seat.
        // Buyers of the same seat get the same lock, buyers of other seats get different locks, so they don't wait.
        ReentrantLock lock = seatLocks.computeIfAbsent(seatId, id-> new ReentrantLock());
        System.out.println("Thread [" + Thread.currentThread().getName() + "] is waiting for the lock of seat " + seatId);

        // Blocks here. If another thread holds this seat's lock, this thread waits until it is released.
        lock.lock();
        try {
            System.out.println("Current thread [ " + Thread.currentThread().getName() + " ] holds the lock of the seat " + seatId);
            // Runs processPurchase inside a database transaction that COMMITS before execute() returns.
            // This matters: the lock is released only after the commit, so the next thread sees the seat as SOLD.
            Ticket ticket = transactionTemplate.execute(status -> processPurchase(email, tournamentId, ticketRequest));
            System.out.println("Seat " + seatId + " sold by thread [" + Thread.currentThread().getName() + "]");
            return ticket;
        }finally {
            // finally always runs, even when processPurchase throws (for example SeatAlreadySoldException),
            // so a failed purchase can never leave the seat locked forever.
            lock.unlock();
            System.out.println("Thread [" + Thread.currentThread().getName() + "] released the lock of the seat " + seatId);
        }
    }


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
    private Ticket processPurchase(String email, Long tournamentId, TicketRequest ticketRequest) {
        Long seatId = ticketRequest.getSeatId();
        log.info("Ticket purchase started: seat={}, thread={}", seatId, Thread.currentThread().getName());

        // Second lock: the database locks the seat row (SELECT ... FOR UPDATE) until this transaction ends.
        // The ReentrantLock protects one running server, and this one protects the database
        Seat seat = seatRepository.findByIdForUpdate(seatId).orElseThrow(
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
           log.warn("Ticket purchase conflict: seat={} already sold, thread={}", seatId, Thread.currentThread().getName());
           throw new SeatAlreadySoldException("Seat " + seat.getSeatNumber() + " is already sold.");
       }
       try {
           Thread.sleep(200);
       } catch (InterruptedException e){
           Thread.currentThread().interrupt();
       }

       seat.setSeatStatus(SeatStatus.SOLD);
       seatRepository.save(seat);

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setTournament(tournament);
        ticket.setSeat(seat);
        ticket.setPrice(seat.getPrice());
        ticketRepository.save(ticket);
        log.info("Ticket purchase succeeded: seat={}, ticket={}, thread={}",
                seatId, ticket.getId(), Thread.currentThread().getName());
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

    @
}
