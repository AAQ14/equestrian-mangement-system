package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON the client or instructor sends to assign a horse to a booking.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HorseAssignmentRequest {
    @NotNull(message = "horse id is required")
    private Long horseId;
}
