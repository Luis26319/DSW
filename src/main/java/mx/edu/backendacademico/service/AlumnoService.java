package mx.edu.backendacademico.service;

// Coordina los casos de uso y conserva las reglas fuera del transporte HTTP.

import java.util.List;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.AlumnoNoEncontradoException;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import mx.edu.backendacademico.repository.AlumnoRepository;
import org.springframework.stereotype.Service;

@Service
public class AlumnoService {
    private final AlumnoRepository repository;

    public AlumnoService(AlumnoRepository repository) {
        this.repository = repository;
    }

    public List<Alumno> listar() {
        // Devuelve una instantánea de los alumnos desde el adaptador activo.
        return repository.buscarTodos();
    }

    public Alumno buscar(Long id) {
        // La ausencia se interpreta como un error de aplicación explícito.
        return repository.buscarPorId(id).orElseThrow(() -> new AlumnoNoEncontradoException(id));
    }

    public Alumno crear(String matricula, String nombre, String correo) {
        // Esta comprobación mejora el mensaje; el almacén vuelve a proteger unicidad.
        if (repository.existePorMatricula(matricula)) {
            throw new MatriculaDuplicadaException(matricula);
        }
        return repository.guardar(new Alumno(null, matricula, nombre, correo, EstatusAlumno.ACTIVO));
    }

    public Alumno actualizar(Long id, String nombre, String correo) {
        // Recupera el estado actual y conserva identidad, matrícula y estatus.
        var actual = buscar(id);
        return repository.guardar(new Alumno(id, actual.matricula(), nombre, correo, actual.estatus()));
    }

    public void eliminar(Long id) {
        // Si no existe, comunica ausencia antes de solicitar la eliminación.
        buscar(id);
        repository.eliminarPorId(id);
    }
}
