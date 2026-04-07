package com.gabriel.identityforge.interfaces.config;

import com.gabriel.identityforge.application.service.AuthenticationService;
import com.gabriel.identityforge.domain.port.out.*;
import com.gabriel.identityforge.infrastructure.messaging.KafkaEventPublisher;
import com.gabriel.identityforge.infrastructure.persistence.adapter.AuditLogAdapter;
import com.gabriel.identityforge.infrastructure.persistence.adapter.PostgresUserRepository;
import com.gabriel.identityforge.infrastructure.persistence.adapter.RefreshTokenRepositoryAdapter;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaAuditLogRepository;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import com.gabriel.identityforge.infrastructure.persistence.repository.JpaUserRepository;
import com.gabriel.identityforge.infrastructure.security.BCryptPasswordHasher;
import com.gabriel.identityforge.infrastructure.security.JwtProvider;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeanConfig {

    @Bean
    public AuthenticationService authenticationService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenProviderPort tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepository,
            AuditLogPort auditLogPort,
            EventPublisherPort eventPublisher
    ) {
        return new AuthenticationService(
                userRepository,
                passwordHasher,
                tokenProvider,
                refreshTokenRepository,
                auditLogPort,
                eventPublisher
        );
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public EventPublisherPort eventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean
    public TokenProviderPort tokenProvider(@Value("${jwt.secret}") String secret) {
        return new JwtProvider(secret);
    }

    @Bean
    public PasswordHasherPort passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    public UserRepositoryPort userRepository(JpaUserRepository jpaRepository) {
        return new PostgresUserRepository(jpaRepository);
    }

    @Bean
    public RefreshTokenRepositoryPort refreshTokenRepository(JpaRefreshTokenRepository repository) {
        return new RefreshTokenRepositoryAdapter(repository);
    }

    @Bean
    public AuditLogPort auditLogPort(JpaAuditLogRepository repository) {
        return new AuditLogAdapter(repository);
    }
}
