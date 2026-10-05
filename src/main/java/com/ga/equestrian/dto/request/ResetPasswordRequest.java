package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "new password is required")
    private String newPassword;
}
