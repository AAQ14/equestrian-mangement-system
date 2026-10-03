package com.ga.equestrian.security;


import com.ga.equestrian.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

    private static final String PURPOSE_CLAIM = "purpose";

    private static final String LOGIN_PURPOSE = "LOGIN";

    private static final String VERIFY_EMAIL_PURPOSE = "VERIFY_EMAIL";

    private static final String ROLE_CLAIM = "role";

    @Value("${jwt-secret}")
    private String secret;

    @Value("${jwt-expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt-verification-expiration-ms}")
    private long jwtVerificationExpirationMs;


    private Key getSigningKey(){
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * Creates the token a user receives after successful login.
     * It carries the email, the role and the LOGIN purpose, and expires after the configured login time.
     *
     * @param email the user's email, stored as the token subject.
     * @param role the user's role, so the server can authorize requests without a database lookup.
     * @return the signed token as string.
     */
    public String generateLoginToken(String email, Role role){
       return Jwts.builder().setSubject(email)
               .claim(ROLE_CLAIM, role.name())
               .claim(PURPOSE_CLAIM, LOGIN_PURPOSE)
               .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
               .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Creates the token placed in email verification link.
     * It has the VERIFY-EMAIL purpose, so it cannot be used to log in,
     * and it expires after the configured verification time.
     *
     * @param email the email of the account being verified.
     * @return the signed token as string.
     */
    public String generateVerificationToken(String email){
        return Jwts.builder().setSubject(email)
                .claim(PURPOSE_CLAIM, VERIFY_EMAIL_PURPOSE)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtVerificationExpirationMs))
                .signWith(getSigningKey(),SignatureAlgorithm.HS256).compact();
    }

    /**
     *  Opens a token and returns its claims; throws if the token is invalid or expired.
     *
     * @param token a signed token.
     * @return the claim of the token/
     */
    private Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Reads the email stored in the token. Call this only after the token has been validated.
     *
     * @param token a signed token created by this class.
     * @return the email stored as the token subject.
     */
    public String getEmailFromToken(String token){
        return getClaims(token).getSubject();
    }

    /**
     * Checks that token has a valid signature, hoa not expired, and was created for the expected purpose.
     * Checking the purpose prevents, for example, a verification token from being used to log in.
     *
     * @param token the token to check.
     * @param purpose the purpose the token must have, such as LOGIN.
     * @return true if the token is valid for that purpose, false otherwise.
     */
    public boolean isTokenValid(String token, String purpose){
        try {
            return purpose.equals(getClaims(token).get(PURPOSE_CLAIM, String.class));
        }catch (JwtException | IllegalArgumentException ex){
            return false;
        }
    }
}
