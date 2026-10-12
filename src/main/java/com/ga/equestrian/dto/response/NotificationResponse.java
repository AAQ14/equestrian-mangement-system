package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The JSON that the API returns, and pushes in real time, for a notification.
 */
@AllArgsConstructor
@Getter
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType notificationType;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
