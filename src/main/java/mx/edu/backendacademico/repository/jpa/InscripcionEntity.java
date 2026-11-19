package mx.edu.backendacademico.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones")
public class InscripcionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private AlumnoEntity alumno;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "materia_id", nullable = false)
    private MateriaEntity materia;
    private LocalDateTime fecha;

    protected InscripcionEntity() { }

    public InscripcionEntity(AlumnoEntity alumno, MateriaEntity materia, LocalDateTime fecha) {
        // El servicio entrega el tiempo: el constructor no consulta un reloj global.
        this.alumno = alumno;
        this.materia = materia;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public MateriaEntity getMateria() { return materia; }
    public LocalDateTime getFecha() { return fecha; }
}
