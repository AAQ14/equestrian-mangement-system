package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.RidingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RidingSessionRepository extends JpaRepository<RidingSession, Long> {
    List<RidingSession> findByInstructorId(Long instructorId);
}
