package mx.edu.backendacademico.exception;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlumnoNoEncontradoException.class)
    ProblemDetail noEncontrado(AlumnoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "ALUMNO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(MatriculaDuplicadaException.class)
    ProblemDetail conflicto(MatriculaDuplicadaException ex) {
        return problema(HttpStatus.CONFLICT, "MATRICULA_DUPLICADA", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        var detalle = problema(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Revisa los campos indicados");
        // Conserva varios errores de un campo sin publicar el valor rechazado.
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new CampoInvalido(e.getField(), e.getDefaultMessage())).toList();
        detalle.setProperty("campos", campos);
        return handleExceptionInternal(ex, detalle, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception ex) {
        String referencia = UUID.randomUUID().toString();
        // Registra una referencia y el tipo, sin cuerpo HTTP, contraseñas ni tokens.
        log.error("Falla no prevista referencia={} tipo={}", referencia, ex.getClass().getName());
        var detalle = problema(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "No fue posible completar la operación");
        detalle.setProperty("referencia", referencia);
        return detalle;
    }

    private ProblemDetail problema(HttpStatus status, String codigo, String mensaje) {
        var detalle = ProblemDetail.forStatusAndDetail(status, mensaje);
        detalle.setTitle(status.getReasonPhrase());
        detalle.setType(URI.create("urn:backend-academico:problema:" + codigo));
        detalle.setProperty("codigo", codigo);
        return detalle;
    }

    public record CampoInvalido(String campo, String mensaje) { }
}