package com.gabriel.identityforge.application.service;

import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;
import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.domain.port.in.LoginUserUseCase;
import com.gabriel.identityforge.domain.port.out.*;

import java.util.List;

public class LoginUserService implements LoginUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuditLogPort auditLogPort;

    public LoginUserService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenProviderPort tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepository,
            AuditLogPort auditLogPort
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogPort = auditLogPort;
    }

    @Override
    public LoginResponse execute(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogPort.log(null, "LOGIN_FAILED", "127.0.0.1", "Unknown");
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogPort.log(user.getId(), "LOGIN_FAILED", "127.0.0.1", "Unknown");
            throw new RuntimeException("Invalid credentials");
        }

        user.updateLastLogin();
        userRepository.save(user);

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        String accessToken = tokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.save(refreshToken, user.getId());

        auditLogPort.log(user.getId(), "LOGIN_SUCCESS", "127.0.0.1", "Unknown");

        return new LoginResponse(accessToken, refreshToken);
    }
}
