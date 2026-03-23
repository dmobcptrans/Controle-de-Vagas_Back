package com.cptrans.petrocarga.infrastructure.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuração de execução assíncrona otimizada para ambientes com pouca memória.
 * 
 * Configurações conservadoras para Railway (350MB RAM):
 * - Core Pool: 1 thread (mínimo sempre ativo)
 * - Max Pool: 2 threads (limite máximo para evitar OOM)
 * - Queue: 50 tarefas (buffer para picos de carga)
 * - Graceful shutdown com timeout de 30s
 * 
 * O pool rejeitará novas tarefas (CallerRunsPolicy) quando cheio,
 * executando-as na thread do caller para evitar perda de emails.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);

	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		
		// Valores conservadores para ambientes com pouca memória (ex: Railway 350MB)
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("EmailAsync-");
		
		// Graceful shutdown: aguarda até 30s para tarefas em andamento terminarem
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		
		// CallerRunsPolicy: se o pool está cheio, executa na thread do caller
		// Isso garante que emails não sejam perdidos mesmo sob carga
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		executor.initialize();
		
		LOGGER.info("AsyncConfig initialized: corePool={}, maxPool={}, queueCapacity={}, threadPrefix={}",
				executor.getCorePoolSize(), executor.getMaxPoolSize(), 
				executor.getQueueCapacity(), "EmailAsync-");
		
		return executor;
	}

	/**
	 * Retorna o executor de tarefas assíncronas configurado nesta classe.
	 * 
	 * @return o executor de tarefas assíncronas configurado
	 */
	@Override
	public Executor getAsyncExecutor() {
		return taskExecutor();
	}

	
/**
 * Retorna um handler de exceções assíncronas que imprime uma mensagem de erro completa no log.
 * 
 * @return um handler de exceções assíncronas que imprime uma mensagem de erro completa no log
 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (Throwable ex, Method method, Object... params) -> {
                    LOGGER.error("=== ASYNC UNCAUGHT EXCEPTION ===");
                    LOGGER.error("Method: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
                    LOGGER.error("Parameters: {}", java.util.Arrays.toString(params));
                    LOGGER.error("Exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
                    LOGGER.error("Full stacktrace:", ex);
                    LOGGER.error("================================");
                };
	}
}
