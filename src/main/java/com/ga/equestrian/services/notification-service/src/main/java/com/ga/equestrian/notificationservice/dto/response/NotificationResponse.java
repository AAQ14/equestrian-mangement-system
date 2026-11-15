package com.ga.equestrian.notificationservice.dto.response;

import com.ga.equestrian.notificationservice.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
