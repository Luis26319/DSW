package mx.edu.backendacademico.exception;

public class OperacionAcademicaException extends RuntimeException {
    private final String codigo;

    public OperacionAcademicaException(String codigo, String mensaje) {
        super(mensaje);
        // El código distingue ausencia, duplicado, baja y falta de cupo.
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
