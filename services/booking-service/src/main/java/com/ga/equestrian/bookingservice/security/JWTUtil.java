package com.ga.equestrian.bookingservice.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Creates and reads the signed JWTs used by the application.
 * Each token carries a purpose claim, so a token made for one job (such as a password reset)
 * cannot be used for another (such as logging in).
 */
@Component
public class JWTUtil {

    @Value("${jwt-secret}")
    private String secret;


    /**
     * Builds the HMAC signing key from the configured secret.
     *
     * @return the key used to sign and verify tokens.
     */
    private Key getSigningKey(){
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * Checks the token and turns it into the signed-in user.
     *
     * @param token the JWT from the Authorization header.
     * @return  the signed-in user, or null if the token is invalid, expired or not a login token.
     */
    public AuthUser toAuthUser(String token){
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                    .parseClaimsJws(token).getBody();
            if(!"LOGIN".equals(claims.get("purpose", String.class))){
                return null;
            }
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            return new AuthUser(userId, claims.getSubject(), role);
        }catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
