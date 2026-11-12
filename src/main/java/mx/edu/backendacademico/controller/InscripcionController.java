package mx.edu.backendacademico.controller;

import java.net.URI;
import java.util.Map;
import mx.edu.backendacademico.dto.InscripcionResponse;
import mx.edu.backendacademico.dto.PaginaResponse;
import mx.edu.backendacademico.service.InscripcionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/inscripciones")
public class InscripcionController {
    private final InscripcionService service;

    public InscripcionController(InscripcionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Map<String, Long>> inscribir(@RequestParam Long alumnoId,
                                                       @RequestParam Long materiaId) {
        if (alumnoId <= 0 || materiaId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificadores positivos requeridos");
        }
        long id = service.inscribir(alumnoId, materiaId);
        // Esta creación se consulta después mediante el historial, sin inventar una ruta GET individual.
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/alumno/{alumnoId}")
    public PaginaResponse<InscripcionResponse> historial(@PathVariable Long alumnoId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @RequestParam(defaultValue = "id") String sort) {
        // Lista blanca de campos y límites explícitos para memoria y consultas.
        if (alumnoId <= 0 || page < 0 || size < 1 || size > 50 ||
                !(sort.equals("id") || sort.equals("fecha"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginación o identificador inválido");
        }
        Sort orden = Sort.by(sort);
        if (!sort.equals("id")) orden = orden.and(Sort.by("id"));
        return service.historial(alumnoId, PageRequest.of(page, size, orden));
    }
}