package com.ga.equestrian.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Security configuration class defines the application security setup, including password hashing with BCrypt.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    /**
     * Filter that reads the JWT from each request and signs the user in.
     */
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * Provides the BCrypt encoder used to hash and check passwords.
     *
     * @return the password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the security rules: CSRF is disabled and no sessions are kept, since every request carries a JWT.
     * The listed routes are public, all other routes require a valid token,
     * and the JWT filter runs before Spring's username and password filter.
     *
     * @param http the security builder provided by Spring.
     * @return the configured security filter chain.
     * @throws Exception if the configuration cannot be built.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->
                        auth.requestMatchers("/auth/users",
                                "/auth/users/login", "/auth/users/register", "/auth/users/verify",  "/auth/users/forgot-password", "/auth/users/verify-reset-code", "/auth/users/reset-password", "/auth/users/login", "/api/sessions/",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll().anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
