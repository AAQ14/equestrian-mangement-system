package com.ga.equestrian.tournamentservice.security;


/**
 * The signed-in user, read from the JWT. This service has no users table,
 * so everything it needs about the user comes from the token.
 *
 * @param userId the id of the user (from the "userId" claim).
 * @param email the email of the user (the token subject).
 * @param role the role of the user, for example CLIENT.
 */
public record AuthUser(Long userId, String email, String role){
}
