package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for reading and saving users.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Find the user by email, returns empty if no user has that email.*/
    Optional<User> findByEmail(String email);
    /** Check whether an account with this email exists it will return true.*/
    boolean existsByEmail(String email);
}
