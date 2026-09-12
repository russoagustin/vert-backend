package com.vert.catalogo.dto;

import com.vert.catalogo.entities.Categoria;

public record CategoriaDto(
        Integer id,
        String nombre,
        Integer orden) {

    public static CategoriaDto fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaDto(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getOrden());
    }

    public Categoria toEntity() {
        Categoria categoria = new Categoria();
        categoria.setId(this.id);
        categoria.setNombre(this.nombre);
        categoria.setOrden(this.orden);
        return categoria;
    }

}
