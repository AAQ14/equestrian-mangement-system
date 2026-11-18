package com.ga.equestrian.notificationservice.dto.event;

import com.ga.equestrian.notificationservice.model.enums.AuditAction;

import java.time.LocalDateTime;


/**
 * The message sent through Kafka when an action should be recorded in the audit log.
 *
 * @param eventId a unique id for the event.
 * @param userId the id of the user who did the action, or null for system actions.
 * @param action what was done.
 * @param entityType the kind of record that changed, for example "Ticket".
 * @param entityId the id of that record.
 * @param description a short text that explains the action.
 * @param occurredAt when it happened.
 */
public record AuditEvent(String eventId, Long userId, AuditAction action, String entityType, Long entityId, String description, LocalDateTime occurredAt) {
}
