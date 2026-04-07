package com.gabriel.identityforge.interfaces.config;

import com.gabriel.identityforge.application.service.AuthenticationService;
import com.gabriel.identityforge.domain.port.in.RegisterUserUseCase;
import com.gabriel.identityforge.domain.port.out.*;
import com.gabriel.identityforge.infrastructure.persistence.adapter.AuditLogAdapter;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaAuditLogRepository;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import com.gabriel.identityforge.infrastructure.persistence.adapter.PostgresUserRepository;
import com.gabriel.identityforge.infrastructure.persistence.adapter.RefreshTokenRepositoryAdapter;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaUserRepository;
import com.gabriel.identityforge.infrastructure.security.BCryptPasswordHasher;
import com.gabriel.identityforge.infrastructure.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UserRepositoryPort userRepository(JpaUserRepository jpaRepository) {
        return new PostgresUserRepository(jpaRepository);
    }

    @Bean
    public PasswordHasherPort passwordHasher() {
        return new BCryptPasswordHasher();
    }

    /*
    @Bean
    public TokenProviderPort tokenProvider() {
        return new JwtProvider();
    }
    */
    // 👇 NOVOS BEANS
    @Bean
    public RefreshTokenRepositoryPort refreshTokenRepository(JpaRefreshTokenRepository jpaRepository) {
        return new RefreshTokenRepositoryAdapter(jpaRepository);
    }

    @Bean
    public AuditLogPort auditLogPort(JpaAuditLogRepository jpaRepository) {
        return new AuditLogAdapter(jpaRepository);
    }

    // Fixed
    @Bean
    public AuthenticationService authenticationService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenProviderPort tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepository,
            AuditLogPort auditLogPort
    ) {
        return new AuthenticationService(
                userRepository,
                passwordHasher,
                tokenProvider,
                refreshTokenRepository,
                auditLogPort
        );
    }

    @Bean
    public TokenProviderPort tokenProvider(@Value("${JWT_SECRET}") String secret) {
        return new JwtProvider(secret);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(AuthenticationService service) {
        return service;
    }

}
