package mx.edu.backendacademico.repository.jpa;

// Relaciona la identidad del objeto administrado con una fila de alumnos.

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;

@Entity
@Table(name = "alumnos")
public class AlumnoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String matricula;
    private String nombre;
    private String correo;
    @Enumerated(EnumType.STRING)
    private EstatusAlumno estatus;

    protected AlumnoEntity() {}
    public AlumnoEntity(Alumno alumno) {
        this.id = alumno.id(); this.matricula = alumno.matricula(); this.nombre = alumno.nombre();
        this.correo = alumno.correo(); this.estatus = alumno.estatus();
    }
    public Alumno toDomain() { return new Alumno(id, matricula, nombre, correo, estatus); }
    public Long getId() { return id; }
}