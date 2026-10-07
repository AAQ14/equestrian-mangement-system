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
}
