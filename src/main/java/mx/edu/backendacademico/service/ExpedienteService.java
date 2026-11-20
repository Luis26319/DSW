package mx.edu.backendacademico.service;

import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.exception.AlumnoNoEncontradoException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ExpedienteService {

    private final AlumnoService alumnoService;

    public ExpedienteService(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    // La identidad sale siempre del Authentication, nunca de la solicitud.
    public Alumno miExpediente(Authentication auth) {
        return alumnoService.listar().stream()
                .filter(a -> a.matricula().equals(auth.getName()))
                .findFirst()
                .orElseThrow(() -> new AlumnoNoEncontradoException(0L)); // o el id correspondiente
    }

    public Alumno expedienteDe(Long id, Authentication auth) {
        Alumno alumno = alumnoService.buscar(id); // 404 si no existe
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean propietario = alumno.matricula().equals(auth.getName());
        if (!admin && !propietario) {
            throw new AccessDeniedException("Expediente ajeno");
        }
        return alumno;
    }
}