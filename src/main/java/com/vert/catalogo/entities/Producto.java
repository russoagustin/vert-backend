package com.vert.catalogo.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Productos")
@NoArgsConstructor
@Getter @Setter
public class Producto {
    @Id
    @Column(name = "idProducto")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false )
    private Categoria categoria;

    private String nombre;
    private BigDecimal precio;

    private BigDecimal precioDescuento;
    private String descripcion;
    private String imgUrl;

}
