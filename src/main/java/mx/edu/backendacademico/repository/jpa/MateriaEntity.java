package mx.edu.backendacademico.repository.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "materias")
public class MateriaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clave;
    private String nombre;
    private int creditos;
    private int cupo;

    protected MateriaEntity() { }

    public MateriaEntity(String clave, String nombre, int creditos, int cupo) {
        // La capacidad pertenece a esta oferta académica simplificada.
        if (creditos <= 0 || cupo <= 0) throw new IllegalArgumentException("Valores no positivos");
        this.clave = clave;
        this.nombre = nombre;
        this.creditos = creditos;
        this.cupo = cupo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCupo() { return cupo; }
}
