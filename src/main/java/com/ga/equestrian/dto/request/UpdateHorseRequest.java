package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.Gender;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
i
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request data for updating a horse.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateHorseRequest {
    private String name;
    private String breed;

    @Min(value = 0, message = "age can't be less than zero")
    @Max(value = 65, message = "age can't be more than 65, is unrealistic")
    private Integer age;

    private Gender gender;
    private HorseStatus horseStatus;
    private SkillLevel skillLevel;
    private MultipartFile image;

}
