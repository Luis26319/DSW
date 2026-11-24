package mx.edu.backendacademico.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import mx.edu.backendacademico.dto.ActualizarAlumnoRequest;
import mx.edu.backendacademico.dto.AlumnoMapper;
import mx.edu.backendacademico.dto.AlumnoResponse;
import mx.edu.backendacademico.dto.CrearAlumnoRequest;
import mx.edu.backendacademico.service.AlumnoIdempotenciaService;
import mx.edu.backendacademico.service.AlumnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alumnos")
public class AlumnoController {
    private final AlumnoService service;
    private final AlumnoIdempotenciaService idempotenciaService;

    public AlumnoController(AlumnoService service, AlumnoIdempotenciaService idempotenciaService) {
        this.service = service;
        this.idempotenciaService = idempotenciaService;
    }

    @GetMapping
    public List<AlumnoResponse> listar() {
        return service.listar().stream().map(AlumnoMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AlumnoResponse buscar(@PathVariable Long id) {
        return AlumnoMapper.toResponse(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<AlumnoResponse> crear(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid CrearAlumnoRequest request,
            Principal principal) {
        String actor = principal != null ? principal.getName() : "anonimo";
        AlumnoResponse respuesta = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotenciaService.crearConIdempotencia(idempotencyKey, actor, request)
                : AlumnoMapper.toResponse(service.crear(request.matricula(), request.nombre(), request.correo()));

        return ResponseEntity.created(URI.create("/api/v1/alumnos/" + respuesta.id())).body(respuesta);
    }

    @PutMapping("/{id}")
    public AlumnoResponse actualizar(
            @PathVariable Long id, @RequestBody @Valid ActualizarAlumnoRequest request) {
        return AlumnoMapper.toResponse(service.actualizar(id, request.nombre(), request.correo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}