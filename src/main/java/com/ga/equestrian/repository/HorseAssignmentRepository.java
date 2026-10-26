package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.HorseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for reading and saving horse assignments.
 */
public interface HorseAssignmentRepository extends JpaRepository<HorseAssignment, Long> {
}
