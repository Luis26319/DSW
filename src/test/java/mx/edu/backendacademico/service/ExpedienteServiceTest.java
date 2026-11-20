package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import mx.edu.backendacademico.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

class ExpedienteServiceTest {
    AlumnoService alumnos = mock(AlumnoService.class);
    ExpedienteService servicio = new ExpedienteService(alumnos);
    Alumno ada = new Alumno(1L, "A001", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO);

    Authentication como(String usuario, String... roles) {
        return new UsernamePasswordAuthenticationToken(usuario, "n/a", AuthorityUtils.createAuthorityList(roles));
    }

    @Test void propietarioPuedeVerSuExpediente() {
        when(alumnos.buscar(1L)).thenReturn(ada);
        assertEquals(ada, servicio.expedienteDe(1L, como("A001", "ROLE_ALUMNO")));
    }

    @Test void ajenoNoPuedeVerElExpediente() {
        when(alumnos.buscar(1L)).thenReturn(ada);
        assertThrows(AccessDeniedException.class,
                () -> servicio.expedienteDe(1L, como("A002", "ROLE_ALUMNO")));
    }

    @Test void administradorPuedeVerCualquierExpediente() {
        when(alumnos.buscar(1L)).thenReturn(ada);
        assertEquals(ada, servicio.expedienteDe(1L, como("admin", "ROLE_ADMIN")));
    }

    @Test void miExpedienteUsaLaIdentidadDelPrincipal() {
        when(alumnos.listar()).thenReturn(List.of(ada));
        assertEquals(ada, servicio.miExpediente(como("A001", "ROLE_ALUMNO")));
    }
}