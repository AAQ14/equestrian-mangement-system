package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for reading and saving audit logs.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * Finds all the audit logs, newest first.
     *
     * @return the audit logs.
     */

    List<AuditLog> findAllByOrderBtCreatedAtDesc();
}
