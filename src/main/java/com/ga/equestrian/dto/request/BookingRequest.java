package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON that clients send to book a riding session.
 * The userId is optional and only used by instructors and admins to book for a client.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "Riding session is required")
    private Long ridingSessionId;

    private Long userId;

}
