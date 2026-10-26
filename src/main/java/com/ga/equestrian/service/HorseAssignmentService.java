package com.ga.equestrian.service;
import com.ga.equestrian.model.entity.Booking;
import com.ga.equestrian.model.entity.HorseAssignment;
import com.ga.equestrian.model.entity.RidingSession;
import com.ga.equestrian.repository.HorseAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Horse service
 */
@Service
@RequiredArgsConstructor
public class HorseAssignmentService {
    private final HorseAssignmentRepository horseAssignmentRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> assignsLock = new ConcurrentHashMap<>();

    public HorseAssignment assignHorse(Booking booking){

    }
}
