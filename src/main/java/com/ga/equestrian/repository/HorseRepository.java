package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Horse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for creating, reading, updating and deleting horses.
 */
public interface HorseRepository extends JpaRepository<Horse, Long> {
}
