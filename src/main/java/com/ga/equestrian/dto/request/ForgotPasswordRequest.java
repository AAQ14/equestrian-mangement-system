package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON that users send when requesting a password reset code.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Email must be a valid address")
    private String email;
}
