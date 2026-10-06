package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.model.enums.Specialization;
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
