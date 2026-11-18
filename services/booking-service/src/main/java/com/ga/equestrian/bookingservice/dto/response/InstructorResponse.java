package com.ga.equestrian.bookingservice.dto.response;

import com.ga.equestrian.bookingservice.model.enums.SkillLevel;
import com.ga.equestrian.bookingservice.model.enums.Specialization;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The instructor data returned.
 */
@AllArgsConstructor
@Getter
public class InstructorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Specialization specialization;
    private Integer experienceYears;
    private SkillLevel skillLevel;
}
