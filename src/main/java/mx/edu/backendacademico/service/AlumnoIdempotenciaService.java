package mx.edu.backendacademico.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import mx.edu.backendacademico.dto.AlumnoMapper;
import mx.edu.backendacademico.dto.AlumnoResponse;
import mx.edu.backendacademico.dto.CrearAlumnoRequest;
import mx.edu.backendacademico.exception.IdempotenciaConflictoException;
import mx.edu.backendacademico.repository.jpa.ClaveIdempotenciaEntity;
import mx.edu.backendacademico.repository.jpa.ClaveIdempotenciaSpringDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlumnoIdempotenciaService {

    public static final String OPERACION_CREAR_ALUMNO = "POST:/api/v1/alumnos";
    private static final String ESTADO_EN_PROCESO = "EN_PROCESO";
    private static final String ESTADO_COMPLETADA = "COMPLETADA";
    private static final int MAX_REINTENTOS = 15;
    private static final long ESPERA_MS = 80L;

    private final ClaveIdempotenciaSpringDataRepository repository;
    private final AlumnoService alumnoService;
    private final ObjectMapper objectMapper;

    public AlumnoIdempotenciaService(
            ClaveIdempotenciaSpringDataRepository repository,
            AlumnoService alumnoService,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.alumnoService = alumnoService;
        this.objectMapper = objectMapper;
    }

    public AlumnoResponse crearConIdempotencia(
            String idempotencyKey, String actor, CrearAlumnoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("La cabecera Idempotency-Key es obligatoria");
        }
        String claveNormalizada = idempotencyKey.trim();
        String actorNormalizado = (actor == null || actor.isBlank()) ? "anonimo" : actor.trim();
        String huella = calcularHuella(request);

        Optional<Long> reservaIdOpt = intentarReservar(claveNormalizada, actorNormalizado, huella);

        if (reservaIdOpt.isEmpty()) {
            return resolverExistente(claveNormalizada, actorNormalizado, huella);
        }

        Long reservaId = reservaIdOpt.get();
        try {
            var creado = alumnoService.crear(request.matricula(), request.nombre(), request.correo());
            AlumnoResponse response = AlumnoMapper.toResponse(creado);
            marcarCompletada(reservaId, creado.id(), response);
            return response;
        } catch (RuntimeException ex) {
            eliminarReservaFallida(reservaId);
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Optional<Long> intentarReservar(String clave, String actor, String huella) {
        try {
            var entity = new ClaveIdempotenciaEntity();
            entity.setIdempotencyKey(clave);
            entity.setOperacion(OPERACION_CREAR_ALUMNO);
            entity.setActor(actor);
            entity.setHuellaContenido(huella);
            entity.setEstado(ESTADO_EN_PROCESO);
            ClaveIdempotenciaEntity guardada = repository.saveAndFlush(entity);
            return Optional.of(guardada.getId());
        } catch (DataIntegrityViolationException ex) {
            return Optional.empty();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void marcarCompletada(Long reservaId, Long alumnoId, AlumnoResponse response) {
        ClaveIdempotenciaEntity entity = repository.findById(reservaId)
                .orElseThrow(() -> new IllegalStateException("No se encontró la reserva de idempotencia"));
        entity.setEstado(ESTADO_COMPLETADA);
        entity.setAlumnoId(alumnoId);
        entity.setCodigoEstado(201);
        entity.setCuerpoRespuesta(serializarRespuesta(response));
        repository.saveAndFlush(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void eliminarReservaFallida(Long reservaId) {
        if (repository.existsById(reservaId)) {
            repository.deleteById(reservaId);
            repository.flush();
        }
    }

    private AlumnoResponse resolverExistente(String clave, String actor, String huella) {
        for (int intento = 0; intento < MAX_REINTENTOS; intento++) {
            ClaveIdempotenciaEntity actual = repository
                    .findByIdempotencyKeyAndOperacionAndActor(clave, OPERACION_CREAR_ALUMNO, actor)
                    .orElse(null);

            if (actual == null) {
                return crearConIdempotencia(clave, actor, reconstruirPeticionDesdeHuellaError());
            }

            if (!actual.getHuellaContenido().equals(huella)) {
                throw new IdempotenciaConflictoException(
                        "La clave de idempotencia ya fue utilizada con un contenido distinto");
            }

            if (ESTADO_COMPLETADA.equals(actual.getEstado()) && actual.getCuerpoRespuesta() != null) {
                return deserializarRespuesta(actual.getCuerpoRespuesta());
            }

            try {
                Thread.sleep(ESPERA_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupción esperando resolución de idempotencia", ie);
            }
        }

        throw new IdempotenciaConflictoException(
                "Ya existe una operación en curso con la misma clave de idempotencia");
    }

    private CrearAlumnoRequest reconstruirPeticionDesdeHuellaError() {
        throw new IllegalStateException("La clave de idempotencia expiró o se liberó durante el reintento");
    }

    public static String calcularHuella(CrearAlumnoRequest request) {
        String canonico = String.join("|",
                normalizarCampo(request.matricula()),
                normalizarCampo(request.nombre()),
                normalizarCampo(request.correo()));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonico.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    private static String normalizarCampo(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String serializarRespuesta(AlumnoResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Error serializando respuesta idempotente", ex);
        }
    }

    private AlumnoResponse deserializarRespuesta(String json) {
        try {
            return objectMapper.readValue(json, AlumnoResponse.class);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Error deserializando respuesta idempotente", ex);
        }
    }
}