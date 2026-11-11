package com.ga.equestrian.bookingservice.mapper;

import com.ga.equestrian.bookingservice.dto.response.InstructorResponse;
import com.ga.equestrian.bookingservice.model.entity.Instructor;
import org.springframework.stereotype.Component;

/**
 * Converts an Instructor entity into an Instructor response.
 */
@Component
public class InstructorMapper {
    public InstructorResponse toResponse(Instructor instructor){
        return new InstructorResponse(instructor.getId(), instructor.getFirstName(), instructor.getLastName(),instructor.getSpecialization() , instructor.getExperienceYears(), instructor.getSkillLevel());
    }
}
