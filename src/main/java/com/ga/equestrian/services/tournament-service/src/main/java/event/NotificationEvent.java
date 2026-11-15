package event;

import com.ga.equestrian.tournamentservice.model.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * The message sent through Kafka when something happens that a user should be told about.
 *
 * @param eventId a unique id, so the consumer never handles the same event twice.
 * @param userId the id of the user who receives the notification.
 * @param type the kind of the notification.
 * @param message the text of the notification.
 * @param occurredAt when it happened.
 */
public record NotificationEvent(String eventId, Long userId, NotificationType type, String message, LocalDateTime occurredAt) {
}
