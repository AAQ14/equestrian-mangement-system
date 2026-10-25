package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.SeatResponse;
import com.ga.equestrian.model.entity.Seat;
import org.springframework.stereotype.Component;

/**
 * Converts a seat entity into its response DTO.
 */
@Component
public class SeatMapper {
    /**
     * Converts a seat entity into its response DTO.
     *
     * @param seat the seat entity.
     * @return the seat response.
     */
    public SeatResponse toResponse(Seat seat){
        return new SeatResponse(seat.getId(), seat.getTournament().getId(), seat.getTournament().getName(), seat.getSeatNumber(), seat.getSeatStatus(), seat.getPrice(), seat.getCreatedAt(), seat.getUpdatedAt());
    }
}
