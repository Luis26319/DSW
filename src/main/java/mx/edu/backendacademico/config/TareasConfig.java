package mx.edu.backendacademico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class TareasConfig {
    // Activa la búsqueda de métodos @Scheduled en beans administrados.
}