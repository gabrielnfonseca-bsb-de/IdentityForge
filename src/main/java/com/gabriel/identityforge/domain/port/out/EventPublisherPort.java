package com.gabriel.identityforge.domain.port.out;

public interface EventPublisherPort {
    void publish(String topic, Object event);
}
