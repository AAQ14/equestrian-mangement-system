package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for reading and saving users.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /** Find the user by email, returns empty if no user has that email.*/
    Optional<User> findByEmail(String email);
    /** Check whether an account with this email exists it will return true.*/
    boolean existsByEmail(String email);
    /**
     * Finds all the users with a given status.
     *
     * @param userStatus the user status, such as ACTIVE or INACTIVE.
     * @return the matching users, or an empty list if there are none.
     */
    List<User> findByUserStatus(UserStatus userStatus);
}
