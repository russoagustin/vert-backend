package com.vert.catalogo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoriaOrdenDto {

    @NotNull(message = "El ID de la subcategoría no puede ser nulo.")
    private Integer id;

    @NotNull(message = "El orden de la subcategoría no puede ser nulo.")
    @Positive(message = "El orden de la subcategoría debe ser mayor a 0.")
    private Integer orden;
}
