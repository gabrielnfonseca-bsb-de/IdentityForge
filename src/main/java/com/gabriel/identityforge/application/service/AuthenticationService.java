package com.gabriel.identityforge.application.service;

import com.gabriel.identityforge.application.dto.request.RefreshTokenRequest;
import com.gabriel.identityforge.application.dto.request.RegisterRequest;
import com.gabriel.identityforge.application.dto.response.RegisterResponse;
import com.gabriel.identityforge.application.dto.response.TokenResponse;
import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.domain.port.in.LoginUserUseCase;
import com.gabriel.identityforge.domain.port.in.RefreshTokenUseCase;
import com.gabriel.identityforge.domain.port.in.RegisterUserUseCase;
import com.gabriel.identityforge.domain.port.out.*;
import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;

import java.util.List;
import java.util.UUID;

public class AuthenticationService implements LoginUserUseCase, RefreshTokenUseCase, RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuditLogPort auditLogPort;

    public AuthenticationService(
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
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        try {
            user.updateLastLogin();
            userRepository.save(user);
        } catch (Exception ignored) {
        }

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

    @Override
    public RegisterResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already registered");
                });

        String passwordHash = passwordHasher.hash(request.getPassword());

        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                passwordHash,
                request.getTenantId()
        );

        userRepository.save(user);

        return new RegisterResponse(
                user.getId(),
                user.getEmail()
        );
    }
}