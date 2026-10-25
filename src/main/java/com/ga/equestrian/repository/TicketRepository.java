package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Ticket;
import com.ga.equestrian.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Repository for reading and saving tickets.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    /**
     * Finds the tickets of a tournament that have one of the given statuses.
     *
     * @param id the id of the tournament.
     * @param ticketStatus the ticket statuses to include, such as PENDING or CONFIRMED.
     * @return the matching tickets.
     */
    List<Ticket> findAllByTournamentIdAndTicketStatusIn(Long id, List<TicketStatus> ticketStatus);

    /**
     * Finds all the tickets that belong to a user.
     *
     * @param id the id of the user.
     * @return the user's tickets.
     */
    List<Ticket> findAllByUserId(Long id);

}
