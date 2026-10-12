package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * The form data a user sends to update a profile.
 * Every field is optional; only the fields that are sent are changed.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateUserRequest {
    @Size(min =1, max=50,message = "First name must be between 1 and 50 characters.")
    private String firstName;

    @Size(min =1, max=50,message = "Last name must be between 1 and 50 characters.")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must contain 8 to 15 digits, optionally starting with +")
    private String phone;

    private MultipartFile image;
}
