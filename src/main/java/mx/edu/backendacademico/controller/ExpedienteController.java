package mx.edu.backendacademico.controller;

import mx.edu.backendacademico.dto.ExpedienteResponse;
import mx.edu.backendacademico.service.ExpedienteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.backendacademico.dto.AlumnoMapper;

@RestController
@RequestMapping("/api/v1")
public class ExpedienteController {

    private final ExpedienteService service;

    public ExpedienteController(ExpedienteService service) {
        this.service = service;
    }

    @GetMapping("/mi-expediente")
    public ExpedienteResponse mio(Authentication auth) {
        return new ExpedienteResponse(AlumnoMapper.toResponse(service.miExpediente(auth)));
    }

    @GetMapping("/alumnos/{id}/expediente")
    public ExpedienteResponse de(@PathVariable Long id, Authentication auth) {
        return new ExpedienteResponse(AlumnoMapper.toResponse(service.expedienteDe(id, auth)));
    }
}