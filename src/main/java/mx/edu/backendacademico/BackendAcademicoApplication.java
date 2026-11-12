package mx.edu.backendacademico;

// Inicia el contexto de Spring y el servidor embebido a partir del paquete raíz.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendAcademicoApplication {
    public static void main(String[] args) {
        // La JVM delega el ensamblado del contexto y del servidor a Spring.
        SpringApplication.run(BackendAcademicoApplication.class, args);
    }
}
