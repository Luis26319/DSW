# Regla de acceso: expediente

## Operaciones
- GET /api/v1/mi-expediente: devuelve el expediente de quien está autenticado.
- GET /api/v1/alumnos/{id}/expediente: devuelve el expediente de un alumno concreto.

## Identidad
Sale del usuario autenticado (Authentication). Ningún parámetro, cuerpo
o encabezado de la solicitud la define.

## Regla
Se puede ver el expediente del alumno X si el usuario es su propietario
(su usuario coincide con la matrícula de X) o si es administrador.
Tener permiso para usar la operación no basta: también hay que ser
propietario del objeto o administrador.

## Resultados
| Quién llama | Qué pide | Resultado |
|---|---|---|
| Propietario (A001) | expediente de A001 | 200 |
| Ajeno (A002) | expediente de A001 | 403 |
| Administrador | expediente de A001 | 200 |
| Sin autenticar | cualquiera | 401 |
| Administrador | un id que no existe | 404 |