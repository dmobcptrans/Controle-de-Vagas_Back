package com.cptrans.petrocarga.application.port.out;

import com.cptrans.petrocarga.domain.events.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
