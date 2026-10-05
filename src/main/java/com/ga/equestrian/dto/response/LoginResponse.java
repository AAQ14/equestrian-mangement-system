package com.ga.equestrian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The first name, last name and token for the logged-in user.
 */
@AllArgsConstructor
@Getter
public class LoginResponse {
    private String firstName;
    private String lastName;
    private String token;
}
