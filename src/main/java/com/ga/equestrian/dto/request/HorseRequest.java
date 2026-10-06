package com.ga.equestrian.dto.request;

import com.ga.equestrian.model.enums.Gender;
import com.ga.equestrian.model.enums.HorseStatus;
import com.ga.equestrian.model.enums.SkillLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HorseRequest {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "breed is required")
    private String breed;

    @NotNull(message = "age is required")
    @Min(value = 0, message = "age can't be less than zero")
    @Max(value = 65, message = "age can't be more than 65, is unrealistic")
    private Integer age;

    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "horse status is required")
    private HorseStatus horseStatus;

    @NotNull(message = "skill level is required")
    private SkillLevel skillLevel;

    private MultipartFile image;
}
