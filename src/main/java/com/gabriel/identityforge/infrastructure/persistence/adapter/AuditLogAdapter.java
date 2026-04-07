package com.gabriel.identityforge.infrastructure.persistence.adapter;

import com.gabriel.identityforge.domain.port.out.AuditLogPort;
import com.gabriel.identityforge.infrastructure.persistence.entity.AuditLogEntity;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaAuditLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuditLogAdapter implements AuditLogPort {

    private final JpaAuditLogRepository repository;

    public AuditLogAdapter(JpaAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void log(UUID userId, String action, String ip, String device) {

        AuditLogEntity entity = new AuditLogEntity();

        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setAction(action);
        entity.setIpAddress(ip);
        entity.setDevice(device);
        entity.setCreatedAt(LocalDateTime.now());

        // ✅ adicionando metadata de forma correta
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ip", ip);
        metadata.put("device", device);
        metadata.put("timestamp", LocalDateTime.now().toString());

        entity.setMetadata(metadata);

        repository.save(entity);
    }
}
