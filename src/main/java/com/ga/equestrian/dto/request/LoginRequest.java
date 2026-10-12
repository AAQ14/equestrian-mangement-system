package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The JSON that clients send when logging into their account.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LoginRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Email must be valid address")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}
