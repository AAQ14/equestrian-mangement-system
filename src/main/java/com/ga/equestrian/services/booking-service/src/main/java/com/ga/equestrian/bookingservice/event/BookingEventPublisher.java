package com.ga.equestrian.bookingservice.event;


import com.ga.equestrian.bookingservice.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sends booking, session and horse events to Kafka, so notification-service can notify the users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    /**
     * The Kafka topic for every booking, session and horse event.
     */
    public static final String TOPIC = "booking-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    /**
     * Publishes a notification event. Inside a transaction it is sent only after the commit.
     *
     * @param userId the id of the user to notify.
     * @param type the kind of the notification.
     * @param message the text of the notification.
     */
    public void publish(Long userId, NotificationType type, String message) {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(), userId, type, message, LocalDateTime.now());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    /**
     * Sends one event. A failure is only logged, so it never breaks a booking.
     *
     * @param event the event to send.
     */
    private void send(NotificationEvent event) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Could not publish event {} for user {}", event.type(), event.userId(), ex);
                        } else {
                            log.info("Published event {} for user {}", event.type(), event.userId());
                        }
                    });
        } catch (Exception ex) {
            log.error("Could not publish event {} for user {}", event.type(), event.userId(), ex);
        }

    }

}
