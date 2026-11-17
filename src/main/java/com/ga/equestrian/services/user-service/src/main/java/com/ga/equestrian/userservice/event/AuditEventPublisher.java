package com.ga.equestrian.userservice.event;

import com.ga.equestrian.userservice.model.enums.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sends audit events to Kafka, so notification-service can save them in the audit log.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventPublisher {
    public static final String TOPIC = "audit-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void record(Long userId, AuditAction action, String entityType, Long entityId, String description ){
        AuditEvent event = new AuditEvent(UUID.randomUUID().toString(), userId, action, entityType, entityId, description, LocalDateTime.now());

        if(TransactionSynchronizationManager.isSynchronizationActive()){
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

    private void send(AuditEvent event){
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.entityId()), event);
            log.info("Published audit {} on {} {}", event.action(), event.entityType(), event.entityId());
        }catch (Exception ex){
            log.error("Could not publish audit {}", event.action(), ex);
        }
    }
}
