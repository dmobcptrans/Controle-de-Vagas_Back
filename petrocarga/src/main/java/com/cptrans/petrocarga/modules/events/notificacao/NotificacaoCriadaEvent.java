package com.cptrans.petrocarga.modules.events.notificacao;

import java.time.Instant;

import com.cptrans.petrocarga.modules.events.DomainEvent;
import com.cptrans.petrocarga.modules.notificacao.entity.Notificacao;

public record NotificacaoCriadaEvent( Notificacao notificacao, Instant occurredOn) implements DomainEvent {
    
    public NotificacaoCriadaEvent(Notificacao notificacao) {
        this(notificacao, Instant.now());
    }

}
