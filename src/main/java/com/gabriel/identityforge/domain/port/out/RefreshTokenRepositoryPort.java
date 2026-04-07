package com.gabriel.identityforge.domain.port.out;

import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    void save(String token, UUID userId);

    boolean isValid(String token);

    void revoke(String token);
}
