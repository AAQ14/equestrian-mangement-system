package com.ga.equestrian.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON the client or instructor sends to update an assigned horse.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HorseAssignmentUpdateRequest {
    private Long horseId;
}
