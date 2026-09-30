package com.ga.equestrian.model.entity;

import com.ga.equestrian.model.enums.Gender;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Horse entity contains descriptions of the horse.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "horses")
@ToString
public class Horse {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String breed;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HorseStatus horseStatus=HorseStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skillLevel;

    @Column
    private String image;
}
