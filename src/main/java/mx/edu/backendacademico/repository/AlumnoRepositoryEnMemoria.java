package mx.edu.backendacademico.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import org.springframework.stereotype.Repository;

@Repository
public class AlumnoRepositoryEnMemoria implements AlumnoRepository {
    private final Map<Long, Alumno> datos = new LinkedHashMap<>();
    private long secuencia;

    @Override
    public synchronized Alumno guardar(Alumno alumno) {
        // 1. Un mismo monitor protege comprobación y escritura como una operación.
        boolean duplicado = datos.values().stream().anyMatch(actual ->
            actual.matricula().equals(alumno.matricula()) &&
            !actual.id().equals(alumno.id()));
        if (duplicado) throw new MatriculaDuplicadaException(alumno.matricula());
        // 2. El incremento comparte el monitor; no necesita un AtomicLong adicional.
        long id = alumno.id() == null ? ++secuencia : alumno.id();
        var guardado = new Alumno(id, alumno.matricula(), alumno.nombre(),
                                  alumno.correo(), alumno.estatus());
        datos.put(id, guardado);
        return guardado;
    }

    @Override
    public synchronized List<Alumno> buscarTodos() {
        // Copia estable: no se expone la colección mutable que vive en el bean.
        return List.copyOf(datos.values());
    }

    @Override
    public synchronized Optional<Alumno> buscarPorId(Long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public synchronized boolean existePorMatricula(String matricula) {
        return datos.values().stream().anyMatch(a -> a.matricula().equalsIgnoreCase(matricula.strip()));
    }

    @Override
    public synchronized void eliminarPorId(Long id) {
        datos.remove(id);
    }
}
