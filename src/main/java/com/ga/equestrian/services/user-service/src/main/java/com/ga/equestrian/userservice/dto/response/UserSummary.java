package com.ga.equestrian.userservice.dto.response;

import com.ga.equestrian.userservice.model.enums.Role;
import com.ga.equestrian.userservice.model.enums.UserStatus;

public record UserSummary(Long id, String firstName, String lastName, String email, UserStatus userStatus, Role role) {
}
