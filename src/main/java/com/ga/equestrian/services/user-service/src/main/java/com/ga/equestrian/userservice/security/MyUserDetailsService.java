package com.ga.equestrian.userservice.security;

import com.ga.equestrian.userservice.model.entity.User;
import com.ga.equestrian.userservice.repository.UserRepository;
import com.ga.equestrian.userservice.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads users from database for Spring Security.
 */

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Finds a user by email and keep it for Spring Security.
     *
     * @param email the email identifying the user whose data is required.
     * @return the matching user as UserDetails object.
     * @throws UsernameNotFoundException if no user has this email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->   new UsernameNotFoundException("User not found.")
        );
        return new MyUserDetails(user);
    }
}
