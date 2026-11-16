package com.ga.equestrian.tournamentservice.event;


import com.ga.equestrian.tournamentservice.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sends ticket and tournament events to Kafka, so notification-service can notify the users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventPublisher {
    /**
     * The Kafka topic for every ticket and tournament event.
     */
    public static final String TOPIC = "ticket-events";

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publish(Long userId, NotificationType type, String message){
        NotificationEvent event = new NotificationEvent(UUID.randomUUID().toString(), userId, type, message, LocalDateTime.now());

        if(TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        }else {
            send(event);
        }
    }

    /**
     * Sends one event. The user id is the key, so all events of one user stay in order.
     *
     */
    private void send(NotificationEvent event){
        kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event)
                .whenComplete((result, ex) -> {
                    if(ex!=null) {
                        log.error("Could not publish event {} for user {}", event.type(), event.userId(), ex);
                    }else{
                        log.info("Published event {} for user {}", event.type(), event.userId());
                    }
                });
    }
}
