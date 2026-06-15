package com.cptrans.petrocarga.modules.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
