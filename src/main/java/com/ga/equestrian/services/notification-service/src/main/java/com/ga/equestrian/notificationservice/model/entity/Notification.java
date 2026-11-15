package com.ga.equestrian.notificationservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.equestrian.notificationservice.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * A notification entity represents an in-app message for a user.
 * It is sent to the connected clients in real time.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
