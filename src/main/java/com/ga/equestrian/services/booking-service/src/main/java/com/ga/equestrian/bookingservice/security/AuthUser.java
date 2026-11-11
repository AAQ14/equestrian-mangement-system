package com.ga.equestrian.bookingservice.security;


/**
 * The signed-in user, read from the JWT on each request.
 * The service gets the user's id, email and role from the token without a database lookup,
 * since the users are stored in user-service.
 *
 * @param userId the id of the user in user-service.
 * @param email the email of the user (the token subject).
 * @param role the role of the user: CLIENT, INSTRUCTOR or ADMIN.
 */
public record AuthUser(Long userId, String email, String role) {
}
