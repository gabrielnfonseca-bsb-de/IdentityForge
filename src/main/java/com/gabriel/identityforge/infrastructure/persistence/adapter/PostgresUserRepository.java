package com.gabriel.identityforge.infrastructure.persistence.adapter;

import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.domain.port.out.UserRepositoryPort;
import com.gabriel.identityforge.infrastructure.persistence.mapper.UserMapper;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaUserRepository;

import java.util.Optional;
import java.util.UUID;

public class PostgresUserRepository implements UserRepositoryPort {

    private final JpaUserRepository jpaRepository;

    public PostgresUserRepository(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        var entity = UserMapper.toEntity(user);
        var saved = jpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }
}
