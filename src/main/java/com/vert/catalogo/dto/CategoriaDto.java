package com.vert.catalogo.dto;

import com.vert.catalogo.entities.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {
    private Integer id;
    private String nombre;

    public static CategoriaDto fromEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaDto(categoria.getId(), categoria.getNombre());
    }

    public Categoria toEntity() {
        Categoria categoria = new Categoria();
        categoria.setId(this.id);
        categoria.setNombre(this.nombre);
        return categoria;
    }
}
