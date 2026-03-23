package com.cptrans.petrocarga.domain.events;

import java.time.Instant;

import com.cptrans.petrocarga.domain.entities.Notificacao;

public record NotificacaoCriadaEvent( Notificacao notificacao, Instant occurredOn) implements DomainEvent {
    
    public NotificacaoCriadaEvent(Notificacao notificacao) {
        this(notificacao, Instant.now());
    }

}
