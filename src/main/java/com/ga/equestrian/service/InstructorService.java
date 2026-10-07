package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.InstructorRequest;
import com.ga.equestrian.dto.response.InstructorResponse;
import com.ga.equestrian.exception.InformationExistException;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.mapper.InstructorMapper;
import com.ga.equestrian.model.entity.Instructor;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.Role;
import com.ga.equestrian.repository.InstructorRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final InstructorMapper instructorMapper;

    /**
     * Creates an instructor profile for an existing user.
     * A client who becomes an instructor gets the INSTRUCTOR role, so role checks work for them.
     *
     * @param request the user id and the instructor details.
     * @return the created instructor as a response DTO.
     * @throws InformationNotFoundException if the user does not exist.
     * @throws InformationExistException    if the user already has an instructor profile.
     */
    @Transactional
    public InstructorResponse addInstructor(InstructorRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new InformationNotFoundException("User with id " + request.getUserId() + " not found."));

        if (instructorRepository.existsByUserId(user.getId())) {
            throw new InformationExistException(
                    "User with id " + user.getId() + " already has an instructor profile.");
        }

        if (user.getRole() == Role.CLIENT) {
            user.setRole(Role.INSTRUCTOR);
        }

        Instructor instructor = new Instructor();
        instructor.setUser(user);
        instructor.setExperienceYears(request.getExperienceYears());
        instructor.setSkillLevel(request.getSkillLevel());
        instructor.setSpecialization(request.getSpecialization());

        Instructor savedInstructor = instructorRepository.save(instructor);
        return instructorMapper.toResponse(savedInstructor);
    }


    /**
     * Returns all instructors as response DTOs.
     * Throws an exception if there are no instructors.
     *
     * @return the list of instructors.
     * @throws InformationNotFoundException if no instructors exist.
     */
    @Transactional
    public List<InstructorResponse> getAllInstructors() {
        List<InstructorResponse> allInstructors =  instructorRepository.findAll()
                .stream()
                .map(instructorMapper::toResponse)
                .toList();
        if(allInstructors.isEmpty()){
            throw new InformationNotFoundException("the list is empty, no instructors");
        }
        return allInstructors;
    }

    /**
     * Returns one instructor by id.
     *
     * @param id the instructor id.
     * @return the instructor as a response DTO.
     * @throws InformationNotFoundException if no instructor has this id.
     */
    @Transactional(readOnly = true)
    public InstructorResponse getInstructorById(Long id){
        Instructor instructor = instructorRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + id + " not found.")
        );
        return instructorMapper.toResponse(instructor);
    }


    /**
     * Updates an instructor's details by id.
     * The linked user cannot be changed.
     *
     * @param id      the instructor id.
     * @param request the new experience, skill level and specialization.
     * @return the updated instructor as a response DTO.
     * @throws InformationNotFoundException if no instructor has this id.
     */
    @Transactional
    public InstructorResponse updateInstructor(Long id, InstructorRequest request){
        Instructor instructor = instructorRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + id + " not found.")
        );
        instructor.setExperienceYears(request.getExperienceYears());
        instructor.setSkillLevel(request.getSkillLevel());
        instructor.setSpecialization(request.getSpecialization());
        instructorRepository.save(instructor);
        return instructorMapper.toResponse(instructor);
    }


    /**
     * Deletes an instructor by id.
     * The user account is kept, and an instructor goes back to the CLIENT role.
     *
     * @param id the instructor id.
     * @return the deleted instructor.
     * @throws InformationNotFoundException if no instructor has this id.
     */
    @Transactional
    public InstructorResponse deleteInstructor(Long id){
        Instructor instructor = instructorRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + id + " not found.")
        );
        User user = instructor.getUser();
        if (user.getRole() == Role.INSTRUCTOR) {
            user.setRole(Role.CLIENT);
        }
        instructorRepository.delete(instructor);
        return instructorMapper.toResponse(instructor);
    }

}
