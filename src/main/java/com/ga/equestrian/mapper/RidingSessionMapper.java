package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.RidingSessionResponse;
import com.ga.equestrian.model.entity.RidingSession;
import org.springframework.stereotype.Component;

/**
 * Converts riding session entity into response
 */
@Component
public class RidingSessionMapper {
    /**
     * Converts a riding session entity into its response DTO.
     *
     * @param ridingSession the session entity.
     * @return the session response.
     */
    public RidingSessionResponse toResponse(RidingSession ridingSession){
        return new RidingSessionResponse(ridingSession.getId(), ridingSession.getInstructor().getId(), ridingSession.getDate(),
                ridingSession.getStartTime(), ridingSession.getEndTime(), ridingSession.getPrice(), ridingSession.getSkillLevel(),
                ridingSession.getSessionStatus(),ridingSession.getCapacity(), ridingSession.getCreatedAt(), ridingSession.getUpdatedAt());
    }
}
