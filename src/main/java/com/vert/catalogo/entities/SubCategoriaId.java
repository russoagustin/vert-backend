package com.vert.catalogo.entities;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoriaId implements Serializable {

    private Integer id;
    private Integer categoria;

}
