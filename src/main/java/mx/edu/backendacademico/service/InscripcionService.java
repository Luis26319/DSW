package mx.edu.backendacademico.service;

import java.time.Clock;
import java.time.LocalDateTime;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.dto.InscripcionResponse;
import mx.edu.backendacademico.dto.PaginaResponse;
import mx.edu.backendacademico.exception.AlumnoNoEncontradoException;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {
    private final AlumnoSpringDataRepository alumnos;
    private final MateriaRepository materias;
    private final InscripcionRepository inscripciones;
    private final Clock reloj;

    public InscripcionService(AlumnoSpringDataRepository alumnos, MateriaRepository materias,
                              InscripcionRepository inscripciones, Clock reloj) {
        this.alumnos = alumnos;
        this.materias = materias;
        this.inscripciones = inscripciones;
        this.reloj = reloj;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long inscribir(Long alumnoId, Long materiaId) {
        // 1. Serializa las admisiones de la misma materia hasta commit/rollback.
        var materia = materias.bloquearPorId(materiaId).orElseThrow(() ->
                new OperacionAcademicaException("MATERIA_NO_ENCONTRADA", "No existe la materia"));
        var alumno = alumnos.findById(alumnoId).orElseThrow(() -> new AlumnoNoEncontradoException(alumnoId));
        // 2. Comprueba reglas después de adquirir el bloqueo común.
        if (alumno.toDomain().estatus() != EstatusAlumno.ACTIVO) {
            throw new OperacionAcademicaException("ALUMNO_INACTIVO", "El alumno está dado de baja");
        }
        if (inscripciones.existsByAlumnoIdAndMateriaId(alumnoId, materiaId)) {
            throw new OperacionAcademicaException("INSCRIPCION_DUPLICADA", "La inscripción ya existe");
        }
        if (inscripciones.countByMateriaId(materiaId) >= materia.getCupo()) {
            throw new OperacionAcademicaException("SIN_CUPO", "La materia no tiene cupo disponible");
        }
        // 3. Un fallo revierte los cambios y libera el bloqueo de base de datos.
        var nueva = new InscripcionEntity(alumno, materia, LocalDateTime.now(reloj));
        return inscripciones.saveAndFlush(nueva).getId();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<InscripcionResponse> historial(Long alumnoId, Pageable pageable) {
        if (!alumnos.existsById(alumnoId)) throw new AlumnoNoEncontradoException(alumnoId);
        // Traduce a DTO dentro de la transacción, con materia cargada por EntityGraph.
        var pagina = inscripciones.findByAlumnoId(alumnoId, pageable).map(i ->
                new InscripcionResponse(i.getId(), i.getMateria().getId(),
                        i.getMateria().getNombre(), i.getFecha()));
        return PaginaResponse.de(pagina);
    }
}