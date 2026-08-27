package com.cptrans.petrocarga.modules.events.conviteMotoristaEmpresa;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.modules.events.DomainEvent;

public record ConviteCriadoEvent (UUID conviteId, OffsetDateTime validoAte, Instant occurredOn) implements DomainEvent {
    public ConviteCriadoEvent(UUID conviteId, OffsetDateTime validoAte) {
        this(conviteId, validoAte, Instant.now());
    }

}