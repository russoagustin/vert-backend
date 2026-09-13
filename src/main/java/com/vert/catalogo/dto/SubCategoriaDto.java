package com.vert.catalogo.dto;

import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.entities.SubCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubCategoriaDto(
        Integer id,
        @NotNull(message = "El ID de la categoría no puede ser nulo.")
        Integer idCategoria,
        @NotBlank(message = "El nombre de la subcategoría no puede ser nulo ni estar vacío.")
        String nombre,
        Integer orden) {

    public static SubCategoriaDto fromEntity(SubCategoria subCategoria) {
        if (subCategoria == null) {
            return null;
        }
        return new SubCategoriaDto(
                subCategoria.getId(),
                subCategoria.getCategoria() != null ? subCategoria.getCategoria().getId() : null,
                subCategoria.getNombre(),
                subCategoria.getOrden());
    }

    public SubCategoria toEntity() {
        SubCategoria subCategoria = new SubCategoria();
        subCategoria.setId(this.id);
        if (this.idCategoria != null) {
            Categoria categoria = new Categoria();
            categoria.setId(this.idCategoria);
            subCategoria.setCategoria(categoria);
        }
        subCategoria.setNombre(this.nombre);
        subCategoria.setOrden(this.orden);
        return subCategoria;
    }
}
