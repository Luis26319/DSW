package mx.edu.backendacademico.repository.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveIdempotenciaSpringDataRepository
        extends JpaRepository<ClaveIdempotenciaEntity, Long> {

    Optional<ClaveIdempotenciaEntity> findByIdempotencyKeyAndOperacionAndActor(
            String idempotencyKey, String operacion, String actor);
}