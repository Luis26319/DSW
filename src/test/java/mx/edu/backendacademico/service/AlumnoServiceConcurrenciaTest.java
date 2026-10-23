package mx.edu.backendacademico.service;

import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AlumnoServiceConcurrenciaTest {

    @Autowired
    private AlumnoService alumnoService;

    @Test
    @DisplayName("Guardar la misma matrícula concurrentemente produce exactamente un éxito y un conflicto")
    void guardarMatriculaConcurrenteDebeGarantizarUnicoExito() throws InterruptedException {
        String matriculaDuplicada = "CONC999";
        Alumno alumnoA = new Alumno(null, matriculaDuplicada, "Ana Test", "ana@u.mx", EstatusAlumno.ACTIVO);
        Alumno alumnoB = new Alumno(null, matriculaDuplicada, "Ana Copia", "copia@u.mx", EstatusAlumno.ACTIVO);

        int hilos = 2;
        CountDownLatch latchInicio = new CountDownLatch(1);
        CountDownLatch latchFin = new CountDownLatch(hilos);

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger conflictos = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(hilos);

        // Hilo 1
        executor.submit(() -> {
            try {
                latchInicio.await(); // Espera la señal de salida sincronizada
                alumnoService.crear(matriculaDuplicada, "Ana Test", "ana@u.mx");
                exitos.incrementAndGet();
            } catch (MatriculaDuplicadaException e) {
                conflictos.incrementAndGet();
            } catch (Exception e) {
                conflictos.incrementAndGet();
            } finally {
                latchFin.countDown();
            }
        });

        // Hilo 2
        executor.submit(() -> {
            try {
                latchInicio.await(); // Espera la señal de salida sincronizada
                alumnoService.crear(matriculaDuplicada, "Ana Copia", "copia@u.mx");
                exitos.incrementAndGet();
            } catch (MatriculaDuplicadaException e) {
                conflictos.incrementAndGet();
            } catch (Exception e) {
                conflictos.incrementAndGet();
            } finally {
                latchFin.countDown();
            }
        });

        // Disparar ambos hilos exactamente al mismo tiempo
        latchInicio.countDown();

        // Esperar la finalización sin pausas arbitrarias (Thread.sleep)
        latchFin.await();
        executor.shutdown();

        // Evidencia requerida: exactamente un registro exitoso y un conflicto controlado
        assertEquals(1, exitos.get(), "Debe haber exactamente un registro guardado con éxito.");
        assertEquals(1, conflictos.get(), "Debe haber exactamente un conflicto por matrícula duplicada.");
    }
}