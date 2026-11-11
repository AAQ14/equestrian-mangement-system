package com.ga.equestrian.bookingservice.service;

import com.ga.equestrian.bookingservice.client.UserClient;
import com.ga.equestrian.bookingservice.dto.request.InstructorRequest;
import com.ga.equestrian.bookingservice.dto.response.InstructorResponse;
import com.ga.equestrian.bookingservice.dto.response.UserSummary;
import com.ga.equestrian.bookingservice.exception.InformationExistException;
import com.ga.equestrian.bookingservice.exception.InformationNotFoundException;
import com.ga.equestrian.bookingservice.exception.InvalidUserActionException;
import com.ga.equestrian.bookingservice.mapper.InstructorMapper;
import com.ga.equestrian.bookingservice.model.entity.Instructor;
import com.ga.equestrian.bookingservice.model.enums.Role;
import com.ga.equestrian.bookingservice.model.enums.UserStatus;
import com.ga.equestrian.bookingservice.repository.InstructorRepository;
import com.ga.equestrian.bookingservice.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorRepository instructorRepository;
    private final UserClient userClient;
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
        UserSummary user = userClient.getUser(request.getUserId());

        if (instructorRepository.existsByUserId(user.id())) {
            throw new InformationExistException(
                    "User with id " + user.id() + " already has an instructor profile.");
        }

        if(user.userStatus()!= UserStatus.ACTIVE){
            throw new InvalidUserActionException("Only active users can become instructors.");
        }

        if (user.role() == Role.CLIENT) {
          userClient.updateUser(user.id(), Role.INSTRUCTOR.name());
        }

        Instructor instructor = new Instructor();
        instructor.setUserId(user.id());
        instructor.setFirstName(user.firstName());
        instructor.setLastName(user.lastName());
        instructor.setExperienceYears(request.getExperienceYears());
        instructor.setSkillLevel(request.getSkillLevel());
        instructor.setSpecialization(request.getSpecialization());

        Instructor savedInstructor = instructorRepository.save(instructor);
/*        if(user.role() == Role.CLIENT){
            userClient.updateRole(Role.INSTRUCTOR);
        }*/
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
        instructorRepository.delete(instructor);
        instructorRepository.flush();// DELETE runs now; if it fails, we stop here.
        userClient.updateUser(instructor.getUserId(), Role.CLIENT.name());// Only runs if the delete worked.
        return instructorMapper.toResponse(instructor);
    }

}
