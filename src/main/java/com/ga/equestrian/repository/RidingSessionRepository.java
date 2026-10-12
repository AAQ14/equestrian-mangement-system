package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for reading and writing riding sessions.
 */
public interface RidingSessionRepository extends JpaRepository<RidingSession, Long> {

    /**
     * Finds all the sessions of an instructor.
     *
     * @param instructorId the id of the instructor.
     * @return the instructor's sessions, or an empty list if there are none.
     */
    List<RidingSession> findByInstructorId(Long instructorId);

    /**
     * Finds all the sessions with a given status.
     *
     * @param status the session status, such as AVAILABLE or FULL.
     * @return the matching sessions, or an empty list if there are none.
     */
    List<RidingSession> findBySessionStatus(SessionStatus status);

    /**
     * Finds all the sessions for a given skill level.
     *
     * @param skillLevel the skill level the session is meant for.
     * @return the matching sessions, or an empty list if there are none.
     */
    List<RidingSession> findBySkillLevel(SkillLevel skillLevel);

    /**
     * Finds all the sessions held on a given date.
     *
     * @param date the session date.
     * @return the matching sessions, or an empty list if there are none.
     */
    List<RidingSession> findByDate(LocalDate date);
}
