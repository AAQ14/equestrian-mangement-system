package com.ga.equestrian.dto.request;


import com.ga.equestrian.model.enums.SkillLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The JSON that clients send to create or update a riding session.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RidingSessionRequest {
    @NotNull(message = "Instructor id is required")
    private Long instructorId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals")
    private BigDecimal price;

    @NotNull(message = "Skill level is required")
    private SkillLevel skillLevel;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity cannot be more than 10")
    private Integer capacity;
}
