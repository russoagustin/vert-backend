package com.vert.catalogo.dto;

import com.vert.catalogo.entities.Categoria;

public record CategoriaDto(
        String nombre,
        Integer orden) {

    public static CategoriaDto fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaDto(
                categoria.getNombre(),
                categoria.getOrden());
    }

    public Categoria toEntity() {
        Categoria categoria = new Categoria();
        categoria.setNombre(this.nombre);
        categoria.setOrden(this.orden);
        return categoria;
    }

}
