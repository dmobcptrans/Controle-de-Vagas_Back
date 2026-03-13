package com.cptrans.petrocarga.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.application.port.out.DomainEventPublisher;
import com.cptrans.petrocarga.domain.event.DomainEvent;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;
    
    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
/**
* Publica um evento de domínio usando o ApplicationEventPublisher do Spring.
* @param event O evento de domínio a ser publicado.
*
*/   
@Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
    
}
