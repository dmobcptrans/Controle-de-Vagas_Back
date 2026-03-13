package com.cptrans.petrocarga.infrastructure.configs.quartz;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

/**
 * Cria e configura o SchedulerFactoryBean para o Quartz, permitindo a integração com o Spring e a injeção de dependências em jobs do Quartz.
 * 
 * @param dataSource a fonte de dados para a conexão ao banco de dados e persistência dos jobs do Quartz
 * @param jobFactory o jobFactory pra permitir a criação de jobs do Quartz
 * 
 * @return o SchedulerFactoryBean configurado
 */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource,
            AutowiringSpringBeanJobFactory jobFactory) {
        
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        
        Properties properties = new Properties();
        
        properties.setProperty("org.quartz.scheduler.instanceName", "QuartzScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "5");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.useProperties", "true");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "false");
        
        schedulerFactoryBean.setQuartzProperties(properties);
        
        return schedulerFactoryBean;
    }
}