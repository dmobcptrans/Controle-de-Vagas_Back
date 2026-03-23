package com.cptrans.petrocarga.domain.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
