package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.HorseAssignmentResponse;
import com.ga.equestrian.model.entity.HorseAssignment;

/**
 * Converts a horse assignment entity into a response.
 */
public class HorseAssignmentMapper {
    /**
     * Converts a horse assignment entity into its DTO response.
     * @param horseAssignment the horse assignment entity.
     * @return the horse assignment response.
     */
    public HorseAssignmentResponse toResponse(HorseAssignment horseAssignment){
        return new HorseAssignmentResponse(horseAssignment.getId(), horseAssignment.getBooking().getId(),
                horseAssignment.getHorse().getId(), horseAssignment.getRidingSession().getId(), horseAssignment.getAssignmentStatus(), horseAssignment.getAssignedAt(), horseAssignment.getUpdatedAt());
    }
}
