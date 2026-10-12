package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    boolean existsByUserId(Long userId);
}
