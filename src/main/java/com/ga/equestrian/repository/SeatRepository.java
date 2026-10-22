package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /**
     *  Counts the seats that belongs to a tournament.
     *
     * @param id the id of the tournament.
     * @return the number of seats in the tournament.
     */
    long countByTournamentId(Long id);
}
