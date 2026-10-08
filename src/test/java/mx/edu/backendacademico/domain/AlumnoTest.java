package mx.edu.backendacademico.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AlumnoTest {
    @Test void bajaConservaIdentidadSinModificarElOriginal() {
        var alumno = new Alumno(1L, " a001 ", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO);
        var baja = alumno.darDeBaja();
        // Comprueba tanto el resultado como la ausencia de efectos laterales.
        assertEquals("A001", baja.matricula());
        assertEquals(alumno.id(), baja.id());
        assertEquals(EstatusAlumno.BAJA, baja.estatus());
        assertEquals(EstatusAlumno.ACTIVO, alumno.estatus());
    }

    @Test void rechazaMatriculaVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Alumno(null, " ", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO));
    }

    @Test void materiaRechazaCreditosNoPositivos() {
        assertThrows(IllegalArgumentException.class, () -> new Materia(null, "M1", "Análisis", 0));
    }

    @Test
    void reactivarConservaIdentidadSinModificarElOriginal() {
        Alumno alumnoOriginal = new Alumno(1L, "A001", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO);
        Alumno alumnoEnBaja = alumnoOriginal.darDeBaja();

        Alumno alumnoReactivado = alumnoEnBaja.reactivar();

        assertEquals(EstatusAlumno.ACTIVO, alumnoReactivado.estatus());
        assertEquals(alumnoEnBaja.id(), alumnoReactivado.id());
        assertEquals(alumnoEnBaja.matricula(), alumnoReactivado.matricula());
        assertEquals(EstatusAlumno.BAJA, alumnoEnBaja.estatus());
        assertEquals(EstatusAlumno.ACTIVO, alumnoOriginal.estatus());
    }

    @Test
    void dosAlumnosConMismoIdYDistintoCorreoShareIdentidad() {
        Alumno alumnoV1 = new Alumno(1L, "A001", "Ada Lovelace", "ada.viejo@u.mx", EstatusAlumno.ACTIVO);
        Alumno alumnoV2 = new Alumno(1L, "A001", "Ada Lovelace", "ada.nuevo@u.mx", EstatusAlumno.ACTIVO);

        assertEquals(alumnoV1.id(), alumnoV2.id());
        assertEquals(alumnoV1.matricula(), alumnoV2.matricula());
        assertNotEquals(alumnoV1.correo(), alumnoV2.correo());
    }
}