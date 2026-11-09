package com.ga.equestrian.tournamentservice.controller;

import com.ga.equestrian.tournamentservice.dto.request.TicketRequest;
import com.ga.equestrian.tournamentservice.dto.response.TicketResponse;
import com.ga.equestrian.tournamentservice.mapper.TicketMapper;
import com.ga.equestrian.tournamentservice.model.entity.Ticket;
import com.ga.equestrian.tournamentservice.security.AuthUser;
import com.ga.equestrian.tournamentservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * @param authUser the signed-in user.
     * @param tournamentId the id of the tournament.
     * @param ticketRequest the id of the seat to buy.
     * @return the created ticket.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{tournamentId}")
    public ResponseEntity<TicketResponse> buyTicket(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long tournamentId , @Valid @RequestBody TicketRequest ticketRequest){
        Ticket ticket = ticketService.buyTicket(authUser.userId(),  tournamentId, ticketRequest);
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
     * @param authUser the signed-in user.
     * @return a list of the user's tickets.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getAllTicketsByUser(@AuthenticationPrincipal AuthUser authUser){
        List<TicketResponse> tickets = ticketService.getAllTicketsByUser(authUser.userId()).stream().map(ticketMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(tickets);
    }

    /**
     * Cancels a ticket of the signed-in user.
     * Returns 200 on success, 403 if the ticket belongs to another user, 404 if it is not found,
     * 409 if it is already cancelled, and 422 if the tournament is not upcoming.
     *
     * @param authUser the signed-in user.
     * @param id the id of the ticket.
     * @return the cancelled ticket.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/cancel/my")
    public ResponseEntity<TicketResponse> cancelTicketByUser(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        Ticket ticket = ticketService.cancelTicketByUser(authUser.userId(), id);
        return ResponseEntity.status(HttpStatus.OK).body(ticketMapper.toResponse(ticket));
    }

    /**
     * Cancels any ticket. Admin only.
     * Returns 200 on success, 403 if the user is not an admin, 404 if the ticket is not found,
     * 409 if it is already cancelled, and 422 if the tournament is not upcoming.
     *
     * @param id the id of the ticket.
     * @return the cancelled ticket.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TicketResponse> cancelTicket(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        Ticket ticket = ticketService.cancelTicket(authUser.userId(), id);
        return ResponseEntity.status(HttpStatus.OK).body(ticketMapper.toResponse(ticket));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/update/my")
    public ResponseEntity<TicketResponse> updateTicket(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody TicketRequest ticketRequest){
        Ticket ticket = ticketService.updateTicket(authUser.userId(), id, ticketRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ticketMapper.toResponse(ticket));
    }
}
