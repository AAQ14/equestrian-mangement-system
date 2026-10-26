package com.ga.equestrian.controller;

import com.ga.equestrian.dto.response.AuditLogResponse;
import com.ga.equestrian.mapper.AuditLogMapper;
import com.ga.equestrian.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Handles the endpoint for reading the audit log. Admin only.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Gets all the audit logs, newest first. Admin only.
     * Returns 200 on success and 403 if the user is not an admin.
     *
     * @return a list of the audit logs.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs(){
        List<AuditLogResponse> auditLogs = auditLogService.getAllAuditLogs().stream().map(auditLogMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(auditLogs);
    }
}
