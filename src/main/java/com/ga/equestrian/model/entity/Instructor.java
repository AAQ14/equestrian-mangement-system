package com.ga.equestrian.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.equestrian.model.enums.SkillLevel;
import com.ga.equestrian.model.enums.Specialization;
import jakarta.persistence.*;
import lombok.*;

/**
 * Instructor entity contains details about the trainer.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "instructors")
@ToString(exclude = "user")
public class Instructor {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialization specialization;

    @Column(nullable = false)
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skillLevel;
}
