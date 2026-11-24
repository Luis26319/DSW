package mx.edu.backendacademico.exception;

public class IdempotenciaConflictoException extends RuntimeException {
    public IdempotenciaConflictoException(String message) {
        super(message);
    }
}