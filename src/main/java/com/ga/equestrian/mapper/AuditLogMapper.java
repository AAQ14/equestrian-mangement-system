package com.ga.equestrian.mapper;

import com.ga.equestrian.dto.response.AuditResponse;
import com.ga.equestrian.model.entity.AuditLog;
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
    public AuditResponse toResponse(AuditLog auditLog){
        Long userId = auditLog.getUser() != null ? auditLog.getUser().getId() : null;
        return new AuditResponse(auditLog.getId(), userId,
                auditLog.getAuditAction(), auditLog.getEntityType(), auditLog.getEntityId(),
                auditLog.getDescription(), auditLog.getCreatedAt());
    }
}
