package com.ga.equestrian.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

/**
 * The JSON that clients send to verify a password reset code.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VerifyResetCodeRequest {
    @NotBlank(message = "email is required")
    @Email(message = "Email must be valid address")
    private String email;

    @NotBlank(message = "code is required")
    @Pattern(regexp = "\\d{6}", message = "code must be 6 digits")
    private String code;
}
