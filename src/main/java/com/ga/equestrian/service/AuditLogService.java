package com.ga.equestrian.service;

import com.ga.equestrian.model.entity.AuditLog;
import com.ga.equestrian.model.enums.AuditAction;
import com.ga.equestrian.repository.AuditLogRepository;
import com.ga.equestrian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Records and reads the audit log.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Adds a row to the audit log.
     *
     * @param email the email of the user who did the action, or null if there is none.
     * @param auditAction the kind of action.
     * @param entityType the kind of record it was done on, e.g. "Seat".
     * @param entityId the id of that record.
     * @param description a short sentence, with ids only and no sensitive data.
     */
    public void record(String email, AuditAction auditAction, String entityType, Long entityId, String description){
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(email == null ? null : userRepository.findByEmail(email).orElse(null));
        auditLog.setAuditAction(auditAction);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        try {
            auditLogRepository.save(auditLog);
        }catch (Exception e){
            log.warn("Could not save audit log: action={}, entity={} {}", auditAction, entityType, entityId);
        }
    }

    /**
     * Gets all the audit logs, newest first.
     *
     * @return a list for the audit logs.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs(){
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

}
