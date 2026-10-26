package com.ga.equestrian.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLog extends JpaRepository<AuditLog, Long> {
}
