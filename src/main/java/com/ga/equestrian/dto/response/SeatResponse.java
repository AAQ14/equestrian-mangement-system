package com.ga.equestrian.dto.response;
import com.ga.equestrian.model.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The JSON sent to the client to show a seat of a tournament.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SeatResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Integer seatNumber;
    private SeatStatus seatStatus;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
