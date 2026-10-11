package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RidingSessionRepository extends JpaRepository<RidingSession, Long> {
    List<RidingSession> findByInstructorId(Long instructorId);
    List<RidingSession> findBySessionStatus(SessionStatus status);
    List<RidingSession> findBySkillLevel(SkillLevel skillLevel);
    List<RidingSession> findByDate(LocalDate date);
}
