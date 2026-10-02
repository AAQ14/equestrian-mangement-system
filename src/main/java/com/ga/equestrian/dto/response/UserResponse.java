package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.Role;
import com.ga.equestrian.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * The user data that is returned to the clients.
 */
@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String firstName, lastName;
    private String email;
    private String phone;
    private String profilePicture;
    private Role role;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
}
