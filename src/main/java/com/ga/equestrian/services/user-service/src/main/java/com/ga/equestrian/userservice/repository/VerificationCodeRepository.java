package com.ga.equestrian.userservice.repository;

import com.ga.equestrian.userservice.model.entity.User;
import com.ga.equestrian.userservice.model.entity.VerificationCode;
import com.ga.equestrian.userservice.model.enums.CodePurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for reading and writing the one-time codes sent to users.
 */
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    /**
     * Finds the code that belongs to a user for a given purpose.
     *
     * @param user the user who owns the code.
     * @param purpose the reason the code was issued.
     * @return the matching code, or an empty Optional if none exists.
     */
    Optional<VerificationCode> findByUserAndPurpose(User user, CodePurpose purpose);
}
