CREATE TABLE claves_idempotencia (
                                     id BIGSERIAL PRIMARY KEY,
                                     idempotency_key VARCHAR(128) NOT NULL,
                                     operacion VARCHAR(64) NOT NULL,
                                     actor VARCHAR(100) NOT NULL,
                                     huella_contenido CHAR(64) NOT NULL,
                                     estado VARCHAR(20) NOT NULL, -- 'EN_PROCESO', 'COMPLETADA'
                                     alumno_id BIGINT REFERENCES alumnos(id),
                                     codigo_estado INTEGER,
                                     cuerpo_respuesta JSONB,
                                     creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                     actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                     CONSTRAINT uk_idempotencia_clave_operacion_actor
                                         UNIQUE (idempotency_key, operacion, actor),
                                     CONSTRAINT chk_idempotencia_estado
                                         CHECK (estado IN ('EN_PROCESO', 'COMPLETADA'))
);