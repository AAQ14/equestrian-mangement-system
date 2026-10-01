package com.ga.equestrian.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.equestrian.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 *  Audit long entity records an important action performed in the application.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
@ToString(exclude = "user")
public class AuditLog {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction auditAction;

    @Column
    private String entityType;

    @Column
    private Long entityId;

    @Column
    private String description;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;
}
