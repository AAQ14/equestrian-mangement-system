package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.TicketResponse;
import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.model.enums.PaymentStatus;

/**
 * Converts ticket entity into response.
 */
public class TicketMapper {
    /**
     * Converts a ticket entity into its response DTO.
     *
     * @param ticket the ticket entity.
     * @return the ticket response.
     */
    public TicketResponse toResponse(Ticket ticket){
        PaymentStatus paymentStatus = ticket.getPayment()!=null? ticket.getPayment().getPaymentStatus() : null;
        return new TicketResponse(ticket.getId(), ticket.getTournament().getId(), ticket.getTournament().getName(), ticket.getSeat().getId(), ticket.getSeat().getSeatNumber(), ticket.getPrice(), paymentStatus, ticket.getTicketStatus(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
