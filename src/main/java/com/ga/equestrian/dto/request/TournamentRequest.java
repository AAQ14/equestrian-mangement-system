package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.TournamentStatus;
import com.ga.equestrian.model.enums.TournamentType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The JSON user send to add a tournament.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TournamentRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "tournament type is required")
    private TournamentType tournamentType;

    @NotNull(message = "date is required")
    @Future(message = "date must be in the future")
    private LocalDate date;

    @NotNull(message = "time is required")
    private LocalTime time;

    @NotBlank(message = "venue is required")
    private String venue;

    @NotNull(message = "number of seats is required")
    @Positive(message = "number of seats must be positive")
    private Integer  numberOfSeats;

    @NotNull(message = "ticket price is required")
    @Positive(message = "ticket price must be positive")
    @Digits(integer = 8, fraction = 2, message = "ticket price must have at most 2 decimal places")
    private BigDecimal ticketPrice;

    @NotBlank(message = "description is required")
    private String description;

}
