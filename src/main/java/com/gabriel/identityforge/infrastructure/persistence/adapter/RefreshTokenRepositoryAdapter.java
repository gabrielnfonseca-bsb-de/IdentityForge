package com.gabriel.identityforge.infrastructure.persistence.adapter;

import com.gabriel.identityforge.domain.port.out.RefreshTokenRepositoryPort;
import com.gabriel.identityforge.infrastructure.persistence.entity.RefreshTokenEntity;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaRefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final JpaRefreshTokenRepository repository;

    public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String token, UUID userId) {

        RefreshTokenEntity entity = new RefreshTokenEntity();

        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setToken(token);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setRevoked(false);

        repository.save(entity);
    }

    @Override
    public boolean isValid(String token) {
        return repository.findByToken(token)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    public void revoke(String token) {
        repository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }
}
