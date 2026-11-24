package mx.edu.backendacademico.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import mx.edu.backendacademico.config.SecurityConfig;
import mx.edu.backendacademico.domain.*;
import mx.edu.backendacademico.service.AlumnoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlumnoController.class)
@Import(SecurityConfig.class)
class SeguridadTest {
    @Autowired MockMvc mvc;
    @MockitoBean AlumnoService service;
    private static final String DATOS = """
        {"matricula":"S1","nombre":"Ada","correo":"ada@u.mx"}
        """;

    @Test void sinCredencialesRecibe401() throws Exception {
        mvc.perform(get("/api/v1/alumnos")).andExpect(status().isUnauthorized());
    }

    @Test void alumnoNoPuedeCrearAunqueTengaCsrf() throws Exception {
        // Token correcto: el 403 de esta prueba se debe al rol.
        mvc.perform(post("/api/v1/alumnos").with(httpBasic("alumno", "alumno123"))
            .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(DATOS))
            .andExpect(status().isForbidden());
    }

    @Test void docenteSinCsrfRecibe403() throws Exception {
        // Rol correcto: esta vez falta la protección de origen de la operación.
        mvc.perform(post("/api/v1/alumnos").with(httpBasic("docente", "docente123"))
            .contentType(MediaType.APPLICATION_JSON).content(DATOS))
            .andExpect(status().isForbidden());
    }

    @Test void docenteConCsrfPuedeCrear() throws Exception {
        when(service.crear(anyString(), anyString(), anyString())).thenReturn(
            new Alumno(1L, "S1", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO));
        mvc.perform(post("/api/v1/alumnos").with(httpBasic("docente", "docente123"))
            .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(DATOS))
            .andExpect(status().isCreated());
    }
}
