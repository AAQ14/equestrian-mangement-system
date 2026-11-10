package com.ga.equestrian.bookingservice.dto.response;

import com.ga.equestrian.bookingservice.model.enums.UserStatus;

public record UserSummary(Long id, String firstName, String lastName, String email, UserStatus userStatus) {
}
