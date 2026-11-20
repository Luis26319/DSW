[1mdiff --git a/src/main/java/mx/edu/backendacademico/controller/AlumnoController.java b/src/main/java/mx/edu/backendacademico/controller/AlumnoController.java[m
[1mindex d9b1b04..8908b34 100644[m
[1m--- a/src/main/java/mx/edu/backendacademico/controller/AlumnoController.java[m
[1m+++ b/src/main/java/mx/edu/backendacademico/controller/AlumnoController.java[m
[36m@@ -38,7 +38,7 @@[m [mpublic class AlumnoController {[m
     @PostMapping[m
     public ResponseEntity<AlumnoResponse> crear(@RequestBody @Valid CrearAlumnoRequest request) {[m
         var creado = service.crear(request.matricula(), request.nombre(), request.correo());[m
[31m-        return ResponseEntity.created(URI.create("/api/v1/alumnos/" + creado.id())).body(AlumnoMapper.toResponse(creado));[m
[32m+[m[32m        return ResponseEntity.ok(AlumnoMapper.toResponse(creado));[m
     }[m
 [m
     // PUT sustituye todos los campos de la representación editable.[m
