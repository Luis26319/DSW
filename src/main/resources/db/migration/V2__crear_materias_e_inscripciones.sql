CREATE TABLE materias (
    id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    creditos INTEGER NOT NULL CHECK (creditos > 0),
    cupo INTEGER NOT NULL CHECK (cupo > 0)
);
CREATE TABLE inscripciones (
    id BIGSERIAL PRIMARY KEY,
    alumno_id BIGINT NOT NULL,
    materia_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL,
    CONSTRAINT uk_inscripcion UNIQUE (alumno_id, materia_id),
    CONSTRAINT fk_inscripcion_alumno FOREIGN KEY (alumno_id) REFERENCES alumnos(id),
    CONSTRAINT fk_inscripcion_materia FOREIGN KEY (materia_id) REFERENCES materias(id)
);
CREATE INDEX ix_inscripciones_materia ON inscripciones(materia_id);
-- Catálogo mínimo del laboratorio; los alumnos se crean mediante HTTP.
INSERT INTO materias(clave, nombre, creditos, cupo)
VALUES ('MAT101', 'Análisis matemático', 8, 30);
