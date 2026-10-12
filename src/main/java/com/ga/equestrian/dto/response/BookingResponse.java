package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The JSON that API returns for a booking.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long ridingSessionId;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
