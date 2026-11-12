package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.concurrent.*;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.MateriaEntity;
import mx.edu.backendacademico.repository.jpa.MateriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class CupoConcurrenteTest {

    @Autowired InscripcionService servicio;   // el bean real
    @Autowired MateriaRepository materias;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager tm;

    @Test
    void unSoloLugarUnaInscripcionYUnRechazo() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();        Long materiaId = materias.save(
                new MateriaEntity("C" + sufijo, "Materia concurrente", 5, 1)).getId();
        Long alumnoA = crearAlumno("A" + sufijo);
        Long alumnoB = crearAlumno("B" + sufijo);

        // Retenedor: sostiene el bloqueo de la fila para poder observar la espera
        CountDownLatch bloqueoTomado = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        Thread retenedor = new Thread(() ->
                new TransactionTemplate(tm).executeWithoutResult(s -> {
                    materias.bloquearPorId(materiaId);
                    bloqueoTomado.countDown();
                    try { soltar.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }), "retenedor");

        List<Thread> hilos = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "admision-" + hilos.size());
            hilos.add(t);
            return t;
        });

        try {
            retenedor.start();
            bloqueoTomado.await();

            CountDownLatch listos = new CountDownLatch(2);
            CountDownLatch salida = new CountDownLatch(1);
            Future<String> fa = pool.submit(tarea(alumnoA, materiaId, listos, salida));
            Future<String> fb = pool.submit(tarea(alumnoB, materiaId, listos, salida));
            listos.await();
            salida.countDown();            // disparo simultáneo

            Thread.sleep(1500);            // ambas quedan esperando el bloqueo

            // EVIDENCIA 1: lado PostgreSQL
            System.out.println("=== pg_stat_activity (sesiones esperando un Lock) ===");
            jdbc.queryForList("""
                select pid, wait_event_type, wait_event,
                       pg_blocking_pids(pid)::text as bloqueado_por, left(query, 70) as consulta
                from pg_stat_activity
                where datname = current_database() and wait_event_type = 'Lock'
                """).forEach(System.out::println);

            // EVIDENCIA 2: lado Java
            System.out.println("=== Estado de los hilos en Java ===");
            for (Thread t : hilos) {
                StackTraceElement[] st = t.getStackTrace();
                System.out.println(t.getName() + " -> " + t.getState()
                        + (st.length > 0 ? " en " + st[0] : ""));
            }

            soltar.countDown();            // el retenedor hace commit y libera la fila

            List<String> resultados = List.of(fa.get(15, TimeUnit.SECONDS), fb.get(15, TimeUnit.SECONDS));
            assertEquals(1, resultados.stream().filter("OK"::equals).count());
            assertEquals(1, resultados.stream().filter("SIN_CUPO"::equals).count());

            Integer persistidas = jdbc.queryForObject(
                    "select count(*) from inscripciones where materia_id = ?", Integer.class, materiaId);
            assertEquals(1, persistidas);
        } finally {
            soltar.countDown();
            pool.shutdownNow();
            jdbc.update("delete from inscripciones where materia_id = ?", materiaId);
            jdbc.update("delete from materias where id = ?", materiaId);
            jdbc.update("delete from alumnos where id in (?, ?)", alumnoA, alumnoB);
        }
    }

    private Callable<String> tarea(Long alumnoId, Long materiaId,
                                   CountDownLatch listos, CountDownLatch salida) {
        return () -> {
            listos.countDown();
            salida.await();
            try {
                servicio.inscribir(alumnoId, materiaId);
                return "OK";
            } catch (OperacionAcademicaException e) {
                return e.getCodigo();      // ajusta al getter real de tu excepción
            }
        };
    }
    private Long crearAlumno(String matricula) {
        return jdbc.queryForObject(
                "insert into alumnos (matricula, nombre, correo, estatus) " +
                        "values (?, ?, ?, 'ACTIVO') returning id",
                Long.class, matricula, "Alumno " + matricula, matricula.toLowerCase() + "@u.mx");
    }
}