-- V2: Añadir restricción para evitar nombres vacíos o nulos en alumnos
ALTER TABLE alumnos
    ADD CONSTRAINT chk_nombre_no_vacio CHECK (nombre IS NOT NULL AND TRIM(nombre) <> '');