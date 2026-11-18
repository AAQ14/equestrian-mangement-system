package com.ga.equestrian.userservice.mapper;

import com.ga.equestrian.userservice.dto.response.UserSummary;
import com.ga.equestrian.userservice.model.entity.User;
import org.springframework.stereotype.Component;

/**
 * Converts a user entity into a summary for the other services.
 */
@Component
public class UserSummaryMapper {
    /**
     * Converts a user entity into a summary.
     * @param user the user entity.
     * @return the user summary.
     */
    public UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getFirstName(), user.getLastName(),  user.getEmail(), user.getUserStatus(), user.getRole());
    }
}
