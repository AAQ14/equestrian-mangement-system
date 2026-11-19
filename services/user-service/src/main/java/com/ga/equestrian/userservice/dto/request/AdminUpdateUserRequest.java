package com.ga.equestrian.userservice.dto.request;

import com.ga.equestrian.userservice.model.enums.Role;
import com.ga.equestrian.userservice.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * The form data an admin sends to update any user.
 * Every field is optional; only the fields that are sent are changed.
 */
@Getter
@Setter
public class AdminUpdateUserRequest {

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters.")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters.")
    private String lastName;

    @Email(message = "Please enter a valid email.")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must contain 8 to 15 digits, optionally starting with +")
    private String phone;

    private Role role;

    private UserStatus userStatus;
}