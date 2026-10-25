package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.TicketRequest;
import com.ga.equestrian.dto.response.TicketResponse;
import com.ga.equestrian.mapper.TicketMapper;
import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.repository.UserRepository;
import com.ga.equestrian.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles the endpoints for buying and viewing tickets.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;


    /**
     * Buy a ticket for a seat of a tournament.
     * Returns 201 on success, 404 if the seat or tournament is not found,
     * 409 if the seat is already sold, and 422 if the tournament is not upcoming or ongoing.
     *
     * @param authentication the signed-in user.
     * @param tournamentId the id of the tournament.
     * @param ticketRequest the id of the seat to buy.
     * @return the created ticket.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{tournamentId}")
    public ResponseEntity<TicketResponse> buyTicket(Authentication authentication, @PathVariable Long tournamentId , @Valid @RequestBody TicketRequest ticketRequest){
        Ticket ticket = ticketService.buyTicket(authentication.getName(),  tournamentId, ticketRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketMapper.toResponse(ticket));
    }

    /**
     * Get all the tickets. Admin only.
     * Returns 200 on success and 403 if the user is not an admin.
     *
     * @return a list of tickets.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(){
        List<TicketResponse> tickets = ticketService.getAllTickets().stream().map(ticketMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(tickets);
    }

    /**
     * Gets the tickets of the signed-in user.
     * Returns 200 on success, with an empty list if there are none.
     *
     * @param authentication the signed-in user.
     * @return a list of the user's tickets.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getAllTicketsByUser(Authentication authentication){
        List<TicketResponse> tickets = ticketService.getAllTicketsByUser(authentication.getName()).stream().map(ticketMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(tickets);
    }

    /**
     * Cancel a ticket.
     *  Returns 200 on success, 403 if the ticket belongs to another user, 404 if it is not found,
     *  409 if it is already cancelled, and 422 if the tournament is not upcoming.
     *
     * @param id the id of the ticket.
     * @return the canceled ticket.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancelTicket(Authentication authentication, @PathVariable Long id) {
        Ticket ticket = ticketService.cancelTicket(authentication.getName(), id);
        return ResponseEntity.status(HttpStatus.OK).body(ticketMapper.toResponse(ticket));
    }
}
