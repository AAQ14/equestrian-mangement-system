package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Seat;
import com.ga.equestrian.model.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    /**
     * finds all the seats of a tournament.
     *
     * @param id the id of the tournament.
     * @return the seats of the tournament.
     */
    List<Seat> findAllByTournamentId(Long id);

    /**
     * find a seat and locks its row until the transaction ends, so other request wait.
     *
     * @param id the id of the seat.
     * @return the locked seat, if it exists.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
