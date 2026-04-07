package com.gabriel.identityforge.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserRegisteredEvent {

    private UUID userId;
    private String email;
    private UUID tenantId;
    private LocalDateTime occurredAt;

    public UserRegisteredEvent(UUID userId, String email, UUID tenantId, LocalDateTime occurredAt) {
        this.userId = userId;
        this.email = email;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
