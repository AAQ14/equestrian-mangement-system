package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON that users send to reset the password.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid address")
    private String email;

    @NotBlank(message = "code is required")
    @Pattern(regexp = "\\d{6}", message = "code must be 6 digits")
    private String code;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters.")
    private String newPassword;
}
