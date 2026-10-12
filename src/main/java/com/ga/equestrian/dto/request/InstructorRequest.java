package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.model.enums.Specialization;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON sends to create an instructor profile.
 *  It refers to an existing user by id.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstructorRequest {

    @NotNull(message = "user is required")
    private Long userId;

    @NotNull(message = "specialization is required")
    private Specialization specialization;

    @NotNull(message = "experience years is required")
    @Min(value = 0, message = "you have to enter a valid experience years")
    private Integer experienceYears;

    @NotNull(message = "skill level is required")
    private SkillLevel skillLevel;


}
