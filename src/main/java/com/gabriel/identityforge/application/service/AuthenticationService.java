package com.gabriel.identityforge.application.service;

import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.request.RefreshTokenRequest;
import com.gabriel.identityforge.application.dto.request.RegisterRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;
import com.gabriel.identityforge.application.dto.response.RegisterResponse;
import com.gabriel.identityforge.application.dto.response.TokenResponse;
import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.domain.port.in.LoginUserUseCase;
import com.gabriel.identityforge.domain.port.in.RefreshTokenUseCase;
import com.gabriel.identityforge.domain.port.in.RegisterUserUseCase;
import com.gabriel.identityforge.domain.port.out.*;
import com.gabriel.identityforge.infrastructure.messaging.event.UserRegisteredEvent;

import java.util.List;
import java.util.UUID;

public class AuthenticationService implements LoginUserUseCase, RefreshTokenUseCase, RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuditLogPort auditLogPort;
    private final EventPublisherPort eventPublisher;

    public AuthenticationService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenProviderPort tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepository,
            AuditLogPort auditLogPort, EventPublisherPort eventPublisher

    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogPort = auditLogPort;
        this.eventPublisher = eventPublisher;
    }

    // =========================
    // LOGIN (LEGADO)
    // =========================

    public LoginResponse login(LoginRequest request) {

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

        auditLogPort.log(
                user.getId(),
                "LOGIN_SUCCESS",
                "127.0.0.1",
                "Unknown"
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    // =========================
    // 🔥 NOVO PADRÃO (USE CASE)
    // =========================
    @Override
    public LoginResponse execute(LoginRequest request) {
        return login(request); // delega para o método existente
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {

        String oldToken = request.getRefreshToken();

        if (!refreshTokenRepository.isValid(oldToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        UUID userId = tokenProvider.extractUserId(oldToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.revoke(oldToken);

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        String newAccessToken = tokenProvider.generateAccessToken(userId, roles);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId);

        refreshTokenRepository.save(newRefreshToken, userId);

        auditLogPort.log(userId, "TOKEN_REFRESH", "127.0.0.1", "Unknown");

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // =========================
    // REGISTER
    // =========================
    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                passwordHasher.hash(request.getPassword()),
                request.getTenantId()
        );

        userRepository.save(user);

        eventPublisher.publish(
                "user.registered",
                new UserRegisteredEvent(
                        user.getId(),
                        user.getEmail(),
                        user.getTenantId(),
                        java.time.LocalDateTime.now()
                )
        );

        return new RegisterResponse(user.getId(), user.getEmail());
    }
}