package com.ga.equestrian.bookingservice.repository;

import com.ga.equestrian.bookingservice.model.entity.Horse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for creating, reading, updating and deleting horses.
 */
public interface HorseRepository extends JpaRepository<Horse, Long> {
    /**
     * Finds a horse and locks its row in the database until the transaction ends,
     * so other requests for the same horse wait.
     *
     * @param id the id of the horse
     * @return the locked horse, if it exists.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Horse h where h.id = :id")
    Optional<Horse> findByIdForUpdate(@Param("id") Long id);
}
