package mx.edu.backendacademico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import mx.edu.backendacademico.exception.AlumnoNoEncontradoException;
import mx.edu.backendacademico.repository.AlumnoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AlumnoServiceTest {
    @Test void unDuplicadoNoDebeIntentarGuardarse() {
        var repository = mock(AlumnoRepository.class);
        when(repository.existePorMatricula("A1")).thenReturn(true);
        var servicio = new AlumnoService(repository);
        // Verifica la regla y su efecto: la operación inválida no escribe.
        assertThrows(MatriculaDuplicadaException.class, () -> servicio.crear("A1", "Ada", "ada@u.mx"));
        verify(repository, never()).guardar(any());
    }

    @Test void unaAusenciaTieneSignificadoDeDominio() {
        var repository = mock(AlumnoRepository.class);
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(AlumnoNoEncontradoException.class, () -> new AlumnoService(repository).buscar(99L));
    }
}
