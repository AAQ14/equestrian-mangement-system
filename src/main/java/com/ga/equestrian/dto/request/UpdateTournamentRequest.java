package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.model.enums.TournamentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JSON data sent by the user for updating a tournament.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateTournamentRequest {

    private String name;
    private TournamentType tournamentType;

    @Future(message = "date must be in the future")
    private LocalDate date;

    private LocalTime time;

    private String venue;

    @Positive(message = "number of seats must be positive")
    private Integer numberOfSeats;

    @Positive(message = "ticket price must be positive")
    @Digits(integer = 8, fraction = 2, message = "ticket price must have at most 2 decimal places")
    private BigDecimal ticketPrice;

    private String description;

}
