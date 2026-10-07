package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.RidingSessionRequest;
import com.ga.equestrian.dto.response.RidingSessionResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.mapper.RidingSessionMapper;
import com.ga.equestrian.model.entity.Instructor;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.repository.InstructorRepository;
import com.ga.equestrian.repository.RidingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RidingSessionService {
    private final RidingSessionRepository ridingSessionRepository;
    private final InstructorRepository instructorRepository;
    private final RidingSessionMapper ridingSessionMapper;


    /**
     * Creates a riding session for an existing instructor.
     *
     * @param request the instructor id and the session details.
     * @return the created session as a response DTO.
     * @throws InformationNotFoundException if the instructor does not exist.
     */
    @Transactional
    public RidingSessionResponse addSession(RidingSessionRequest request){
        Instructor instructor = instructorRepository.findById(request.getInstructorId()).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + request.getInstructorId() + " not found."));

        RidingSession session = new RidingSession();
        session.setInstructor(instructor);
        session.setDate(request.getDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setPrice(request.getPrice());
        session.setSkillLevel(request.getSkillLevel());
        session.setCapacity(request.getCapacity());

        RidingSession savedSession = ridingSessionRepository.save(session);
        return ridingSessionMapper.toResponse(savedSession);
    }

    /**
     * Returns all riding sessions as response DTOs.
     * Throws an exception if there are no sessions.
     *
     * @return the list of sessions.
     * @throws InformationNotFoundException if no sessions exist.
     */
    @Transactional(readOnly = true)
    public List<RidingSessionResponse> getAllSessions(){
        List<RidingSessionResponse> allSessions = ridingSessionRepository.findAll()
                .stream()
                .map(ridingSessionMapper::toResponse)
                .toList();
        if(allSessions.isEmpty()){
            throw new InformationNotFoundException("the list is empty, no riding sessions");
        }
        return allSessions;
    }


    /**
     * Returns one riding session by id.
     *
     * @param id the session id.
     * @return the session as a response DTO.
     * @throws InformationNotFoundException if no session has this id.
     */
    @Transactional(readOnly = true)
    public RidingSessionResponse getSessionById(Long id){
        RidingSession session = ridingSessionRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Riding session with id " + id + " not found.")
        );

        return ridingSessionMapper.toResponse(session);
    }

    /**
     * Updates a riding session's details by id.
     * The instructor can be changed to another existing instructor.
     * @param id  the session id.
     * @param request the new instructor and session details.
     * @return the updated session as a response DTO.
     * @throws InformationNotFoundException if the session or the instructor does not exit.
     */
    @Transactional
    public RidingSessionResponse updateSession(Long id, RidingSessionRequest request){
        RidingSession ridingSession = ridingSessionRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("Riding session with id " + id + " not found.")
        );

        Instructor instructor = instructorRepository.findById(request.getInstructorId()).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + request.getInstructorId() + " not found."));


        ridingSession.setInstructor(instructor);
        ridingSession.setDate(request.getDate());
        ridingSession.setStartTime(request.getStartTime());
        ridingSession.setEndTime(request.getEndTime());
        ridingSession.setPrice(request.getPrice());
        ridingSession.setSkillLevel(request.getSkillLevel());
        ridingSession.setCapacity(request.getCapacity());
        ridingSessionRepository.save(ridingSession);
        return ridingSessionMapper.toResponse(ridingSession);
    }

    /**
     * Deletes a riding session by id.
     *
     * @param id the session id.
     * @return the deleted session.
     * @throws InformationNotFoundException if no session has the id.
     */
    @Transactional
    public RidingSessionResponse deleteSession(Long id) {
        RidingSession session = ridingSessionRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Riding session with id " + id + " not found.")
        );
        ridingSessionRepository.delete(session);
        return ridingSessionMapper.toResponse(session);
    }
}
