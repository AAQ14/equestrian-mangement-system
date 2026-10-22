package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Tournament;
import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.model.enums.TournamentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for reading and saving tournaments.
 */
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    /**
     * find a tournament by the status, such as: ONGOING, UPCOMING, COMPLETED, CANCELLED.
     *
     * @param status the status.
     * @return a list of tournaments by the status.
     */
    List<Tournament> findByTournamentStatus(TournamentStatus status);

    /**
     * find a tournament by the tournament type, such as: SHOW_JUMPING,
     *     EVENTING,
     *     DRESSAGE,
     *     SHOWING,
     *     POLO,
     *     RACING,
     *     VAULTING,
     *     REINING,
     *     ENDURANCE
     *
     * @param tournamentType the type of the tournament.
     * @return a list of tournament that equal to the type inserted.
     */
    List<Tournament> findByTournamentType(TournamentType tournamentType);

    /**
     * find tournaments by specific date.
     *
     * @param date the date.
     * @return a list of tournament that equal to the date inserted.
     */
    List<Tournament> findByDate(LocalDate date);

    /**
     *
     * @param status
     * @param date
     * @return
     */
    List<Tournament> findByTournamentStatusAndDateGreaterThanEqual(TournamentStatus status, LocalDate date);

    /**
     *
     * @param name
     * @param date
     * @return
     */
    Boolean existsByNameAndDate(String name, LocalDate date);
}
