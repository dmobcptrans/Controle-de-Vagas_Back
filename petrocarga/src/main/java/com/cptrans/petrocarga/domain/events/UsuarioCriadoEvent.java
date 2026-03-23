package com.cptrans.petrocarga.domain.events;

import java.time.Instant;

public record UsuarioCriadoEvent (String email, String codigo, String randomPassword, Instant occurredOn)  implements DomainEvent{
    public UsuarioCriadoEvent(String email, String codigo, String randomPassword) {
        this(email, codigo, randomPassword, Instant.now());
    }
}
