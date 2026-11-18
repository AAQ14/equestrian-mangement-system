package com.ga.equestrian.notificationservice.mapper;

import com.ga.equestrian.notificationservice.dto.response.AuditLogResponse;
import com.ga.equestrian.notificationservice.model.entity.AuditLog;
import org.springframework.stereotype.Component;

/**
 * Converts an audit log entity into its response DTO.
 */
@Component
public class AuditLogMapper {
    /**
     * Converts an audit log entity into its response DTO.
     *
     * @param auditLog the audit log entity.
     * @return the audit log response.
     */
    public AuditLogResponse toResponse(AuditLog auditLog){
        return new AuditLogResponse(auditLog.getId(), auditLog.getUserId(),
                auditLog.getAuditAction(), auditLog.getEntityType(), auditLog.getEntityId(),
                auditLog.getDescription(), auditLog.getCreatedAt());
    }
}
