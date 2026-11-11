package com.ga.equestrian.bookingservice.repository;

import com.ga.equestrian.bookingservice.model.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    boolean existsByUserId(Long userId);
}
