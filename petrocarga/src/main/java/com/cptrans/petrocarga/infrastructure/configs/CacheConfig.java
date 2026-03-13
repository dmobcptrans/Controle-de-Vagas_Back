package com.cptrans.petrocarga.infrastructure.configs;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

/**
 * Cria um gerenciador de cache que utiliza um mapa concorrent para armazenar
 * os dados de cache.
 *
 * Os caches criados por esse gerenciador serão:
 * - dashboard-kpi: cache para os KPIs do dashboard
 * - dashboard-vehicle-types: cache para os tipos de veículos do dashboard
 * - dashboard-districts: cache para os distritos do dashboard
 * - dashboard-origins: cache para as origens do dashboard
 * - dashboard-summary: cache para o resumo do dashboard
 */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "dashboard-kpi",
            "dashboard-vehicle-types",
            "dashboard-districts",
            "dashboard-origins",
            "dashboard-summary"
        );
    }
}
