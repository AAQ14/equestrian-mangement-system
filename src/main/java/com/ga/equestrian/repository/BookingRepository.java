package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookingRepository extends JpaRepository<Booking, Long> {
//    long countByRidingSessionI

}
