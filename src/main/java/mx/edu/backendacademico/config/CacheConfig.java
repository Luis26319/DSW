package mx.edu.backendacademico.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        // Difiere put/evict/clear hasta commit cuando hay una transacción activa.
        // Esta caché local es de laboratorio: no tiene TTL ni coordinación entre réplicas.
        return new TransactionAwareCacheManagerProxy(
                new ConcurrentMapCacheManager("alumnos", "alumnosLista"));
    }
}