package mx.edu.backendacademico.dto;

// Define los campos públicos de una respuesta de alumno.
import mx.edu.backendacademico.domain.EstatusAlumno;
public record AlumnoResponse(Long id, String matricula, String nombre, String correo, EstatusAlumno estatus) {}
