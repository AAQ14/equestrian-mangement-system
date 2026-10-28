package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * The JSON that API returns for a horse assignment.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HorseAssignmentResponse {
    private Long id;
    private Long bookingId;
    private Long horseId;
    private Long ridingSessionId;
    private AssignmentStatus assignmentStatus;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
