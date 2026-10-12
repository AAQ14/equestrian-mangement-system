package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.NotificationResponse;
import com.ga.equestrian.model.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Converts notification entity into response.
 */
@Component
public class NotificationMapper {
    /**
     * Converts a notification entity into its response DTO.
     *
     * @param notification the notification entity.
     * @return the notification response.
     */
    public NotificationResponse toResponse(Notification notification){
        return new NotificationResponse(notification.getId(), notification.getMessage(), notification.getNotificationType(), notification.getIsRead(), notification.getCreatedAt());
    }
}
