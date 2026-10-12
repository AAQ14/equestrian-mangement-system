package com.ga.equestrian.dto.response;


import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * The JSON that the API returns for a riding session.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RidingSessionResponse {
    private Long id;
    private Long instructorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private SkillLevel skillLevel;
    private SessionStatus sessionStatus;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
