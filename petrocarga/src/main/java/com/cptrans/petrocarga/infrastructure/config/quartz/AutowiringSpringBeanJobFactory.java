package com.cptrans.petrocarga.infrastructure.config.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

@Component
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    private transient AutowireCapableBeanFactory beanFactory;

/**
 * Configura o SpringBeanJobFactory para permitir a injeção de dependências em jobs do Quartz.
 * 
 * @param context o contexto Spring
 * @return void (nenhuma resposta é enviada)
 */
    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

/**
 * Cria uma instância de um job do Quartz, para permitir a injeção de dependências em jobs do Quartz.
 * 
 * @param bundle o pacote de trigger do Quartz
 * @return Object
 * @throws Exception
 */
    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
