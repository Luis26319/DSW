package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import mx.edu.backendacademico.dto.AlumnoResponse;
import mx.edu.backendacademico.dto.CrearAlumnoRequest;
import mx.edu.backendacademico.exception.IdempotenciaConflictoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AlumnoIdempotenciaTest {

    @Autowired
    private AlumnoIdempotenciaService idempotenciaService;

    @Test
    void dosSolicitudesSimultaneasConMismaClaveYContenidoDevuelvenElMismoResultado() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String clave = "idem-sim-" + sufijo;
        var payload = new CrearAlumnoRequest("MAT-" + sufijo, "Grace Hopper", "grace." + sufijo + "@u.mx");

        var inicio = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<AlumnoResponse> tarea = () -> {
                inicio.await(5, TimeUnit.SECONDS);
                return idempotenciaService.crearConIdempotencia(clave, "docente", payload);
            };

            Future<AlumnoResponse> f1 = pool.submit(tarea);
            Future<AlumnoResponse> f2 = pool.submit(tarea);

            inicio.countDown();

            AlumnoResponse r1 = f1.get(10, TimeUnit.SECONDS);
            AlumnoResponse r2 = f2.get(10, TimeUnit.SECONDS);

            assertNotNull(r1.id());
            assertEquals(r1.id(), r2.id());
            assertEquals(payload.matricula().toUpperCase(), r2.matricula());
            assertEquals("Grace Hopper", r2.nombre());
        }
    }

    @Test
    void mismaClaveConDistintoContenidoRechazaConConflicto() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String clave = "idem-conf-" + sufijo;
        var original = new CrearAlumnoRequest("MAT-A-" + sufijo, "Alan Turing", "alan.a." + sufijo + "@u.mx");
        var alterado = new CrearAlumnoRequest("MAT-A-" + sufijo, "Alan M. Turing", "alan.b." + sufijo + "@u.mx");

        AlumnoResponse primero = idempotenciaService.crearConIdempotencia(clave, "docente", original);
        assertNotNull(primero.id());

        assertThrows(IdempotenciaConflictoException.class, () ->
                idempotenciaService.crearConIdempotencia(clave, "docente", alterado));
    }
}