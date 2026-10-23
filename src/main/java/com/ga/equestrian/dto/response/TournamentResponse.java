package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.model.enums.TournamentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *  The JSON returned to the client to describe a tournament.
 */
@AllArgsConstructor
@Getter
@ToString
public class TournamentResponse {
    private Long id;
    private String name;
    private TournamentType tournamentType;
    private LocalDate date;
    private LocalTime time;
    private String venue;
//    private Integer numberOfSeats;
    private TournamentStatus tournamentStatus;
    private BigDecimal ticketPrice;
    private String description;

}
