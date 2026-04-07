package com.gabriel.identityforge.domain.port.out;

import java.util.UUID;

public interface AuditLogPort {

    void log(UUID userId, String action, String ip, String device);
}
