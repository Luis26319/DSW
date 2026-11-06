package mx.edu.backendacademico.dto;

// Define la parte editable; matrícula, identidad y estatus no se alteran por PUT.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarAlumnoRequest(
    @NotBlank @Size(min = 3, max = 120) String nombre,
    @NotBlank @Email @Size(max = 160) String correo
) {}
