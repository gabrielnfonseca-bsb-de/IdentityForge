package com.gabriel.identityforge.domain.port.out;

import java.util.List;
import java.util.UUID;

public interface TokenProviderPort {

    String generateAccessToken(UUID userId, List<String> roles);

    String generateRefreshToken(UUID userId);

    UUID extractUserId(String token);

    List<String> extractRoles(String token);
}