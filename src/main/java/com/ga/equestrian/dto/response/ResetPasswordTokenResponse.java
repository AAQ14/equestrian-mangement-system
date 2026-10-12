package com.ga.equestrian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The token returned after successfully verifying a password reset code.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordTokenResponse {
    private String resetToken;
}
