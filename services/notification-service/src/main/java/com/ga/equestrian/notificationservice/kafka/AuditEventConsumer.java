package com.ga.equestrian.notificationservice.kafka;

import com.ga.equestrian.bookingservice.event.AuditEvent;
import com.ga.equestrian.notificationservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reads the audit events from kafka and saves each one in the audit log.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {
    private final AuditLogService auditLogService;


    @KafkaListener(topics="audit-events", groupId = "notification-service-audit",properties = "spring.json.value.default.type=com.ga.equestrian.notificationservice.dto.event.AuditEvent" )
    public void handle(AuditEvent event){
        log.info("Received audit {} on {} {}", event.action(), event.entityType(), event.entityId());
        auditLogService.record(event.userId(), event.action(), event.entityType(),
                event.entityId(), event.description());
    }
}
