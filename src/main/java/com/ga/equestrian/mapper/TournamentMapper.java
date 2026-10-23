package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.TournamentResponse;
import com.ga.equestrian.model.entity.Tournament;
import org.springframework.stereotype.Component;

/**
 * converts tournament entity into response.
 */
@Component
public class TournamentMapper {
    /**
     *  Converts a tournament session entity into its response DTO.
     *
     * @param tournament the tournament entity.
     * @return the tournament response.
     */
    public TournamentResponse toResponse(Tournament tournament){
        return new TournamentResponse(tournament.getId(), tournament.getName(), tournament.getTournamentType(), tournament.getDate(), tournament.getTime(), tournament.getVenue(), tournament.getTournamentStatus(), tournament.getTicketPrice(), tournament.getDescription());
    }
}
