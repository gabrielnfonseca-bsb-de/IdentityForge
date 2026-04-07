package com.gabriel.identityforge.application.service;

import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.request.RefreshTokenRequest;
import com.gabriel.identityforge.application.dto.request.RegisterRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;
import com.gabriel.identityforge.application.dto.response.RegisterResponse;
import com.gabriel.identityforge.application.dto.response.TokenResponse;
import com.gabriel.identityforge.domain.model.Role;
import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.domain.port.out.AuditLogPort;
import com.gabriel.identityforge.domain.port.out.PasswordHasherPort;
import com.gabriel.identityforge.domain.port.out.RefreshTokenRepositoryPort;
import com.gabriel.identityforge.domain.port.out.TokenProviderPort;
import com.gabriel.identityforge.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private AuditLogPort auditLogPort;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UUID userId;
    private UUID tenantId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        user = new User(userId, "gabriel@email.com", "hashed-password", tenantId);
        user.addRole(new Role(1L, "USER", "Default role"));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("novo@email.com");
        request.setPassword("123456");
        request.setTenantId(tenantId);

        when(userRepository.findByEmail("novo@email.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("123456")).thenReturn("hashed-password");

        RegisterResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("novo@email.com", response.getEmail());

        verify(userRepository).findByEmail("novo@email.com");
        verify(passwordHasher).hash("123456");
        verify(userRepository).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void shouldThrowWhenRegisteringExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("gabriel@email.com");
        request.setPassword("123456");
        request.setTenantId(tenantId);

        when(userRepository.findByEmail("gabriel@email.com")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.register(request));

        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository).findByEmail("gabriel@email.com");
        verify(passwordHasher, never()).hash(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail("gabriel@email.com");
        request.setPassword("123456");

        when(userRepository.findByEmail("gabriel@email.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("123456", "hashed-password")).thenReturn(true);
        when(tokenProvider.generateAccessToken(eq(userId), anyList())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(refreshTokenRepository).save("refresh-token", userId);
        verify(auditLogPort).log(eq(userId), eq("LOGIN_SUCCESS"), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("gabriel@email.com");
        request.setPassword("senha-errada");

        when(userRepository.findByEmail("gabriel@email.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("senha-errada", "hashed-password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.login(request));

        assertEquals("Invalid credentials", ex.getMessage());
        verify(tokenProvider, never()).generateAccessToken(any(), anyList());
        verify(refreshTokenRepository, never()).save(anyString(), any());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String oldToken = "old-refresh-token";

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(oldToken);

        when(refreshTokenRepository.isValid(oldToken)).thenReturn(true);
        when(tokenProvider.extractUserId(oldToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(eq(userId), anyList())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("new-refresh-token");

        TokenResponse response = authenticationService.refresh(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(refreshTokenRepository).revoke(oldToken);
        verify(refreshTokenRepository).save("new-refresh-token", userId);
        verify(auditLogPort).log(eq(userId), eq("TOKEN_REFRESH"), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenRefreshTokenIsInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(refreshTokenRepository.isValid("invalid-token")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authenticationService.refresh(request));

        assertEquals("Invalid refresh token", ex.getMessage());
        verify(tokenProvider, never()).extractUserId(anyString());
        verify(refreshTokenRepository, never()).revoke(anyString());
    }
}
