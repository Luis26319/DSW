package mx.edu.backendacademico.dto;

import java.time.LocalDateTime;

// La representación contiene valores, nunca proxies de Hibernate.
public record InscripcionResponse(Long id, Long materiaId, String materia,
                                  LocalDateTime fecha) { }