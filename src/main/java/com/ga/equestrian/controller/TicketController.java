package com.ga.equestrian.controller;

import com.ga.equestrian.dto.request.TicketRequest;
import com.ga.equestrian.dto.response.TicketResponse;
import com.ga.equestrian.mapper.TicketMapper;
import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.model.entity.User;
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
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;

    /**
     *  Buy a ticket for a seat.
     *
     * @param ticketRequest the id of the seat to buy.
     * @return the created ticket.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<TicketResponse> buyTicket(Authentication authentication, @Valid @RequestBody TicketRequest ticketRequest){
        Ticket ticket = ticketService.buyTicket(authentication.getName(), ticketRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketMapper.toResponse(ticket));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(){
        List<TicketResponse> tickets = ticketService.getAllTickets().stream().map(ticketMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(tickets);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getTicketByUser")
    public ResponseEntity<List<TicketResponse>> getAllTicketsByUser(Authentication authentication){
        List<TicketResponse> tickets = ticketService.getAllTicketsByUser(authentication.getName()).stream().map(ticketMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(tickets);
    }


}
