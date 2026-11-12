package mx.edu.backendacademico.dto;

// Define exclusivamente los campos que el cliente puede enviar al crear.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearAlumnoRequest(
    @NotBlank @Size(max = 30) String matricula,
@NotBlank @Size(min = 3, max = 120) String nombre,
@NotBlank @Email @Size(max = 160) String correo
) {}
