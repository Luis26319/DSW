-- El esquema aplica las mismas condiciones básicas que el dominio Java.
CREATE TABLE alumnos (
                         id BIGSERIAL PRIMARY KEY,
                         matricula VARCHAR(30) NOT NULL,
                         nombre VARCHAR(120) NOT NULL,
                         correo VARCHAR(160) NOT NULL,
                         estatus VARCHAR(20) NOT NULL CHECK (estatus IN ('ACTIVO', 'BAJA')),
                         CONSTRAINT uk_alumnos_matricula UNIQUE (matricula),
                         CONSTRAINT ck_matricula_canonica CHECK (matricula = UPPER(TRIM(matricula))),
                         CONSTRAINT ck_matricula_no_vacia CHECK (LENGTH(TRIM(matricula)) > 0)
);