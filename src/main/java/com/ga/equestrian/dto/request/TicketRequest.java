package com.ga.equestrian.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON user send to book a seat.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketRequest {
    @NotNull(message = "seat id is required")
    private Long seatId;
}
