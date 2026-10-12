package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.UserResponse;
import com.ga.equestrian.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts a User entity into a UserResponse so the entity is never exposed to the clients.
 */
@Component
public class UserMapper {
    /**
     * Converts a user entity into its response DTO.
     *
     * @param user the user entity.
     * @return the user response, without the password.
     */
    public UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getProfilePicture(), user.getRole(), user.getUserStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
