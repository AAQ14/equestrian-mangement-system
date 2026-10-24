package com.ga.equestrian.dto.response;
import com.ga.equestrian.model.enums.PaymentStatus;
import com.ga.equestrian.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The JSON sent to the user to show a ticket's details.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long seatId;
    private Integer seatNumber;
    private BigDecimal price;
    private PaymentStatus paymentStatus;
    private TicketStatus ticketStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
