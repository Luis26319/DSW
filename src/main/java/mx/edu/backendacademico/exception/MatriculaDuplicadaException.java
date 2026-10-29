package mx.edu.backendacademico.exception;

// Representa un conflicto con la unicidad de matrícula.

public class MatriculaDuplicadaException extends RuntimeException {
    public MatriculaDuplicadaException(String matricula) {
        super("Ya existe la matrícula " + matricula);
    }
}
