package com.ga.equestrian.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.equestrian.model.enums.CodePurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a one-time code sent to a user by email.
 * The code is stored as a hash, and it expires at a set time.
 * The attempts counter records wrong tries so the code cannot be guessed.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "verification_codes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "purpose"}))
@ToString(exclude = {"user", "codeHash"})
public class VerificationCode {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodePurpose purpose;

    @Column(nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attempts = 0 ;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
