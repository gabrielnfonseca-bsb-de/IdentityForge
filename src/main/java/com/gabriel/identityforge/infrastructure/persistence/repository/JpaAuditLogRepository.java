package com.gabriel.identityforge.infrastructure.persistence.repository;

import com.gabriel.identityforge.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
