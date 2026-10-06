package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.Gender;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HorseResponse {
    private Long id;
    private String name;
    private String breed;
    private Integer age;
    private Gender gender;
    private HorseStatus horseStatus;
    private SkillLevel skillLevel;
    private String image;
}
