package com.gabriel.identityforge.domain.port.out;

import com.gabriel.identityforge.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);
}
