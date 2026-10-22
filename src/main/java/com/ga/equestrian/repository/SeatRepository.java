package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
