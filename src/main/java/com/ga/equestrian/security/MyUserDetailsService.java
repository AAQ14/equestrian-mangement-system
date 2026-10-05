package com.ga.equestrian.security;

import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * loads users from database for Spring Security.
 *
 */

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    UserRepository userRepository;

    /**
     * find a user by email and keep it for Spring Security.
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
