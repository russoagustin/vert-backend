package com.vert.catalogo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Categorias")
@NoArgsConstructor
@Getter
@Setter
public class Categoria {

    @Id
    @Column(name = "idCategoria")
    private Integer id;

    private String nombre;
    private Integer orden;
}
