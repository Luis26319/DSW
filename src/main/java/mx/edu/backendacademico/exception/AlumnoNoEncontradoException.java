package mx.edu.backendacademico.exception;

// Distingue la ausencia de un alumno de una falla de infraestructura.

public class AlumnoNoEncontradoException extends RuntimeException {
    public AlumnoNoEncontradoException(Long id) {
        super("No existe el alumno con id " + id);
    }
}
