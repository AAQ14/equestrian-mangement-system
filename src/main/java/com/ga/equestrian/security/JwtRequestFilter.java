package com.ga.equestrian.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per request. Reads the JWT from the Authorization header and, if it is a valid
 * LOGIN token, signs the user in by placing their details in the security context.
 * Requests without a valid token continue unauthenticated and are rejected later by the security rules.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    /**
     * Extracts the token from an "Authorization: Bearer token" header.
     *
     * @param request the incoming request.
     * @return the token, or null if the header is missing or not a Bearer header.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    /**
     * Validates the token, loads the user and sets the authentication for this request.
     * Errors while reading the token are logged and the request continues unauthenticated.
     *
     * @param request the incoming request.
     * @param response the outgoing response.
     * @param filterChain the rest of the filter chain.
     * @throws ServletException if the chain fails.
     * @throws IOException if reading or writing fails.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            System.out.println("jwt: ==> " + jwt);

            if (jwt != null && jwtUtil.isTokenValid(jwt, "LOGIN")) {

                String email = jwtUtil.getEmailFromToken(jwt);

                System.out.println("email: ==> " + email);

                UserDetails userDetails =
                        myUserDetailsService.loadUserByUsername(email);

                System.out.println(
                        "userDetails: ==> " + userDetails.getUsername()
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
    }
}