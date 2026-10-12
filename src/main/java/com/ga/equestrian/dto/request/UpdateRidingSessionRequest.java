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
 * The JSON that clients send to update a riding session
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateRidingSessionRequest {
    private Long instructorId;

    @FutureOrPresent(message = "Date cannot be in the past")
    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals")
    private BigDecimal price;

    private SkillLevel skillLevel;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10, message = "Capacity cannot be more than 10")
    private Integer capacity;
}
