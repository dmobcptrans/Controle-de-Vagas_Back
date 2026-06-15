package com.cptrans.petrocarga.modules.events;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
