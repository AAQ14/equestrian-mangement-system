package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.InstructorRequest;
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
    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final InstructorMapper instructorMapper;

    public InstructorResponse addInstructor(InstructorRequest request){

        User user = userRepository.findById(request.getUserId()).orElseThrow(
                ()->new InformationNotFoundException("User with this id " + request.getUserId() + " not found")
        );

        Instructor newInstructor = new Instructor();
        newInstructor.setUser(user);
        newInstructor.setExperienceYears(request.getExperienceYears());
        newInstructor.setSkillLevel(request.getSkillLevel());
        newInstructor.setSpecialization(request.getSpecialization());
        instructorRepository.save(newInstructor);
        return instructorMapper.toResponse(newInstructor);
    }
}
