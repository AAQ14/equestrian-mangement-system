package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts a User entity into a UserResponse so the entity is never exposed to the clients.
 */
@Component
public class UserMapper {
    public UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getProfilePicture(), user.getRole(), user.getUserStatus(), user.getCreatedAt());
    }
}
