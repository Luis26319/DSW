package mx.edu.backendacademico;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contexto;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BackendAcademicoApplicationTests {
    // Comprueba ensamblado y mapeo JPA. PostgreSQL y migraciones se prueban en U09.
    @Test void cargaElContextoConBaseEfimera() { }
}