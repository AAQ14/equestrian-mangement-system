package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.InstructorResponse;
import com.ga.equestrian.model.entity.Instructor;
import org.springframework.stereotype.Component;

/**
 * Converts an Instructor entity into an Instructor response.
 */
@Component
public class InstructorMapper {
    public InstructorResponse toResponse(Instructor instructor){
        return new InstructorResponse(instructor.getUser().getId(), instructor.getUser().getFirstName(), instructor.getUser().getLastName(),instructor.getSpecialization() , instructor.getExperienceYears(), instructor.getSkillLevel());
    }
}
