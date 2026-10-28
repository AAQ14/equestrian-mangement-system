package com.ga.equestrian.service;

import com.ga.equestrian.dto.request.RidingSessionRequest;
import com.ga.equestrian.dto.request.UpdateRidingSessionRequest;
import com.ga.equestrian.dto.response.RidingSessionResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.exception.InvalidSessionException;
import com.ga.equestrian.mapper.RidingSessionMapper;
import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.entity.Instructor;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.model.enums.BookingStatus;
import com.ga.equestrian.model.enums.NotificationType;
import com.ga.equestrian.model.enums.SessionStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.repository.BookingRepository;
import com.ga.equestrian.repository.HorseAssignmentRepository;
import com.ga.equestrian.repository.InstructorRepository;
import com.ga.equestrian.repository.RidingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RidingSessionService {
    private final RidingSessionRepository ridingSessionRepository;
    private final InstructorRepository instructorRepository;
    private final RidingSessionMapper ridingSessionMapper;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final HorseAssignmentRepository horseAssignmentRepository;


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
     * Returns riding sessions as response DTOs, optionally filtered by status, skill level or date.
     * Only one filter is applied at a time, in this order: status, skill level, date.
     * Throws an exception if there are no sessions.
     *
     * @param status the session status to filter by, or null.
     * @param skillLevel the skill level to filter by, or null.
     * @param date the session date to filter by, or null.
     * @return the list of sessions.
     * @throws InformationNotFoundException if no sessions match.
     */
    @Transactional(readOnly = true)
    public List<RidingSessionResponse> getAllSessions(SessionStatus status, SkillLevel skillLevel, LocalDate date){
        List<RidingSession> allSessions;
        if(status!=null){
            allSessions= ridingSessionRepository.findBySessionStatus(status);
        }else if(skillLevel != null){
            allSessions = ridingSessionRepository.findBySkillLevel(skillLevel);
        }else if(date != null){
            allSessions = ridingSessionRepository.findByDate(date);
        }else{
            allSessions = ridingSessionRepository.findAll();
        }
        List<RidingSessionResponse> sessions = allSessions
                .stream()
                .map(ridingSessionMapper::toResponse)
                .toList();
        if(sessions.isEmpty()){
            throw new InformationNotFoundException("the list is empty, no riding sessions");
        }
        return sessions;
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
    public RidingSessionResponse updateSession(Long id, UpdateRidingSessionRequest request){
        RidingSession ridingSession = ridingSessionRepository.findById(id).orElseThrow(
                ()-> new InformationNotFoundException("Riding session with id " + id + " not found.")
        );


        if(request.getDate()!=null){
            ridingSession.setDate(request.getDate());
        }
        if(request.getInstructorId()!=null){ Instructor instructor = instructorRepository.findById(request.getInstructorId()).orElseThrow(
                () -> new InformationNotFoundException("Instructor with id " + request.getInstructorId() + " not found."));
            ridingSession.setInstructor(instructor);
        }
        if(request.getStartTime()!=null){
            ridingSession.setStartTime(request.getStartTime());
        }
        if(request.getEndTime()!=null){
            ridingSession.setEndTime(request.getEndTime());
        }
        if(request.getPrice()!=null){
            ridingSession.setPrice(request.getPrice());
        }
        if(request.getSkillLevel()!=null){
            ridingSession.setSkillLevel(request.getSkillLevel());
        }
        if(request.getCapacity()!=null){
            ridingSession.setCapacity(request.getCapacity());
        }
        ridingSessionRepository.save(ridingSession);
        notificationService.createAndSend(ridingSession.getInstructor().getUser(),
                "Your session on " + ridingSession.getDate() + " was updated.",
                NotificationType.SESSION_UPDATED);
        return ridingSessionMapper.toResponse(ridingSession);
    }

    /**
     * Cancels a riding session. Only the session's instructor can cancel it.
     * The session is kept with the CANCELLED status, its pending and confirmed bookings are cancelled,
     * their horse assignments are removed so the horses are free again,
     * and every booked user is notified on the thread pool.
     *
     * @param email the email of the signed-in instructor.
     * @param id the session id.
     * @return the cancelled session.
     * @throws InformationNotFoundException if no session has the id.
     * @throws AccessDeniedException if the session belongs to another instructor.
     * @throws InvalidSessionException if the session is already cancelled or completed.
     */
    @Transactional
    public RidingSessionResponse cancelSession(String email, Long id) {

        RidingSession session = ridingSessionRepository.findById(id).orElseThrow(
                () -> new InformationNotFoundException("Riding session with id " + id + " not found.")
        );

        boolean isInstructorSession = session.getInstructor().getUser().getEmail().equals(email);
        if(!isInstructorSession){
            throw new AccessDeniedException("You can only cancel your own sessions.");
        }

        if(session.getSessionStatus().equals(SessionStatus.COMPLETED) || session.getSessionStatus().equals(SessionStatus.CANCELLED)){
            throw new InvalidSessionException("A cancelled or completed session cannot be cancelled.");
        }

        session.setSessionStatus(SessionStatus.CANCELLED);
        List<Booking> bookings = bookingRepository.findAllByRidingSessionIdAndBookingStatusIn(id, List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING));
        for(Booking booking: bookings){
            booking.setBookingStatus(BookingStatus.CANCELLED);

            horseAssignmentRepository.findByBookingId(booking.getId()).ifPresent(horseAssignmentRepository::delete);
        }
        bookingRepository.saveAll(bookings);
        ridingSessionRepository.save(session);

        for(Booking booking: bookings){
            notificationService.sendInBackground(booking.getUser().getId(),
                    "Your booking for " + session.getDate() + " was cancelled because the session was cancelled."
                    , NotificationType.SESSION_CANCELLED);
        }

        notificationService.sendInBackground(session.getInstructor().getUser().getId(),
                "Your session on " + session.getDate() + " was cancelled, and " + bookings.size() + " bookings were cancelled"
                , NotificationType.SESSION_CANCELLED);

        log.info("Session {} cancelled, {} bookings cancelled, thread={}",
                id, bookings.size(), Thread.currentThread().getName());

        return ridingSessionMapper.toResponse(session);
    }
}
