package com.cptrans.petrocarga.modules.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
