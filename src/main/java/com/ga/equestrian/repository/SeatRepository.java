package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.model.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /**
     *  Counts the seats that belongs to a tournament.
     *
     * @param id the id of the tournament.
     * @return the number of seats in the tournament.
     */
    long countByTournamentId(Long id);

    /**
     * Finds all the seats of a tournament that have a given status.
     *
     * @param id the id of the tournament.
     * @param seatStatus the seat status, such as AVAILABLE or SOLD.
     * @return a list of the seats.
     */
    List<Seat> findAllByTournamentIdAndSeatStatus(Long id, SeatStatus seatStatus);
}
