package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.model.enums.Specialization;

/**
 * The instructor data returned.
 */
public class InstructorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Specialization specialization;
    private Integer experienceYears;
    private SkillLevel skillLevel;
}
