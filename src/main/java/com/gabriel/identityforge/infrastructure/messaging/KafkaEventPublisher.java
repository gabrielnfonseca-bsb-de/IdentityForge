package com.gabriel.identityforge.infrastructure.messaging;

import com.gabriel.identityforge.domain.port.out.EventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, Object event) {
        kafkaTemplate.send(topic, event);
    }
}
