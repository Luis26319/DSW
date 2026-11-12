package mx.edu.backendacademico.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CicloDeVida {
    private static final Logger log = LoggerFactory.getLogger(CicloDeVida.class);
    private final Clock reloj;

    public CicloDeVida(Clock reloj) {
        // Primero Spring resuelve Clock y después llama al constructor.
        this.reloj = reloj;
    }

    @PostConstruct
    void iniciar() {
        // La dependencia ya está asignada; evita aquí trabajo remoto prolongado.
        log.info("Bean inicializado en {}", reloj.instant());
    }

    @PreDestroy
    void cerrar() {
        // Observa esta llamada al detener ordenadamente la aplicación.
        log.info("Cierre del bean de demostración");
    }
}
