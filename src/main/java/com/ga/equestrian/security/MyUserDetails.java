package com.ga.equestrian.security;

import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


/**
 *  Adapt the User entity to the UserDetails that Spring Security understand.
 */
@RequiredArgsConstructor
public class MyUserDetails implements UserDetails {
    /** The user entity this object wraps. */
    @Getter
    private final User user;


    /**
     * Returns the user's role as a Spring authority, with the ROLE_ prefix that hasRole() expects.
     *
     * @return a list with one authority, such as ROLE_CLIENT.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /**
     * Returns the user's hashed password.
     *
     * @return the password hash.
     */
    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the value Spring uses as the username, which is the user's email.
     *
     * @return the user's email.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Accounts never expire in this application.
     *
     * @return always true.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Accounts are never locked by Spring Security; blocking is done through the user status.
     *
     * @return always true.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Passwords never expire in this application.
     *
     * @return always true.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Only users whose status is ACTIVE are enabled.
     *
     * @return true if the user is ACTIVE, false if pending or inactive.
     */
    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE == user.getUserStatus();
    }
}
