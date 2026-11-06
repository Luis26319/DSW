package mx.edu.backendacademico.repository.jpa;

import java.util.List;
import java.util.Optional;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import mx.edu.backendacademico.repository.AlumnoRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("jpa")
@Transactional(readOnly = true)
public class AlumnoJpaAdapter implements AlumnoRepository {
    private final AlumnoSpringDataRepository repository;

    public AlumnoJpaAdapter(AlumnoSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Alumno guardar(Alumno alumno) {
        try {
            // Flush envía SQL antes de abandonar este método y permite traducir el conflicto.
            return repository.saveAndFlush(new AlumnoEntity(alumno)).toDomain();
        } catch (DataIntegrityViolationException ex) {
            // Traduce únicamente la restricción conocida; otras fallas conservan su causa.
            Throwable causa = ex;
            while (causa != null) {
                if (causa instanceof ConstraintViolationException restriccion &&
                        "uk_alumnos_matricula".equalsIgnoreCase(restriccion.getConstraintName())) {
                    throw new MatriculaDuplicadaException(alumno.matricula());
                }
                causa = causa.getCause();
            }
            throw ex;
        }
    }

    @Override
    public List<Alumno> buscarTodos() {
        // Este listado didáctico se limita a conjuntos pequeños de alumnos.
        return repository.findAll().stream().map(AlumnoEntity::toDomain).toList();
    }

    @Override
    public Optional<Alumno> buscarPorId(Long id) {
        return repository.findById(id).map(AlumnoEntity::toDomain);
    }

    @Override
    public boolean existePorMatricula(String matricula) {
        return repository.existsByMatriculaIgnoreCase(matricula.strip());
    }

    @Override
    @Transactional
    public void eliminarPorId(Long id) {
        repository.deleteById(id);
    }
}