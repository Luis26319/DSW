package mx.edu.backendacademico.dto;

// Convierte el valor de dominio a la representación pública sin acceder a la base.
import mx.edu.backendacademico.domain.Alumno;
public final class AlumnoMapper {
    private AlumnoMapper() {}
    public static AlumnoResponse toResponse(Alumno alumno) {
        // Sólo se publican los campos declarados por el DTO de salida.
        return new AlumnoResponse(alumno.id(), alumno.matricula(), alumno.nombre(), alumno.correo(), alumno.estatus());
    }
}
