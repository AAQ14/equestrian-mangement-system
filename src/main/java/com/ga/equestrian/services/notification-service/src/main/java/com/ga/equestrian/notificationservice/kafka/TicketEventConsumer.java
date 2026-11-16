package com.ga.equestrian.notificationservice.kafka;

import com.ga.equestrian.notificationservice.dto.event.NotificationEvent;
import com.ga.equestrian.notificationservice.model.entity.Notification;
import com.ga.equestrian.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**z
 * Reads the ticket and tournament events from Kafka and turns into one notification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventConsumer {
    public final NotificationService notificationService;

    @KafkaListener(topics = "ticket-events", groupId = "notification-service")
    public void handle(NotificationEvent event) {
        log.info("Received event {} for user {} (eventId={})", event.type(), event.userId(), event.eventId());
        notificationService.createAndSend(event.userId(), event.message(), event.type());
    }
}
