package com.cptrans.petrocarga.modules.events.usuario;

import java.time.Instant;

import com.cptrans.petrocarga.modules.events.DomainEvent;

public record UsuarioCriadoEvent (String email, String codigo, String randomPassword, Instant occurredOn)  implements DomainEvent{
    public UsuarioCriadoEvent(String email, String codigo, String randomPassword) {
        this(email, codigo, randomPassword, Instant.now());
    }
}