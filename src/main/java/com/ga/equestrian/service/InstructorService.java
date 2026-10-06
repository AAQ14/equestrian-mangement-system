package com.ga.equestrian.service;

import com.ga.equestrian.dto.response.InstructorResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.mapper.InstructorMapper;
import com.ga.equestrian.model.entity.Instructor;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.repository.InstructorRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private InstructorRepository instructorRepository;
    private UserRepository userRepository;
    private final InstructorMapper instructorMapper;

    public InstructorResponse addInstructor(Instructor instructor){

        User user = userRepository.findById(instructor.getUser().getId()).orElseThrow(
                ()->new InformationNotFoundException("User with this id " + instructor.getUser().getId() + " not found")
        );

        Instructor newInstructor = new Instructor();
        newInstructor.setUser(user);
        newInstructor.setExperienceYears(instructor.getExperienceYears());
        newInstructor.setSkillLevel(instructor.getSkillLevel());
        newInstructor.setSpecialization(instructor.getSpecialization());

        return instructorMapper.toResponse(newInstructor);
    }
}
