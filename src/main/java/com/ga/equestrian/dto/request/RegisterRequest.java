package com.ga.equestrian.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

/**
 * The JSON that clients send to register.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    @Size(min =1, max=50,message = "First name must be between 1 and 50 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min =1, max=50,message = "Last name must be between 1 and 50 characters.")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters.")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must contain 8 to 15 digits, optionally starting with +")
    private String phone;
}
