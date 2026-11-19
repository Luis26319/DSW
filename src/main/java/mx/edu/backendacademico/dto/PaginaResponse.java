package mx.edu.backendacademico.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano,
                                long totalElementos, int totalPaginas) {
    public PaginaResponse {
        // Copia defensiva para que la lista de salida no cambie después.
        contenido = List.copyOf(contenido);
    }

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(pagina.getContent(), pagina.getNumber(),
            pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
