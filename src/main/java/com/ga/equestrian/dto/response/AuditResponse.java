package com.ga.equestrian.dto.response;

import com.ga.equestrian.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The JSON sent to the admin to show one audit log row.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditResponse {
    private Long id;
    private Long userId;
    private AuditAction auditAction;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime createdAt;
}
