package com.vert.catalogo.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.entities.Producto;
import com.vert.catalogo.entities.SubCategoria;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoDto(
        Integer id,

        @NotNull(message = "El ID de la categoría no puede ser nulo.")
        Integer idCategoria,

        @NotNull(message = "El ID de la subcategoría no puede ser nulo.")
        Integer idSubCategoria,

        @NotBlank(message = "El nombre del producto no puede ser nulo ni estar vacío.")
        String nombre,

        @NotNull(message = "El precio del producto no puede ser nulo.")
        @PositiveOrZero(message = "El precio del producto debe ser mayor o igual a 0.")
        BigDecimal precio,

        @PositiveOrZero(message = "El precio de descuento no puede ser negativo.")
        BigDecimal precioDescuento,

        String descripcion,

        String imgUrl,

        @PositiveOrZero(message = "La cantidad debe ser mayor o igual a 0.")
        Integer cantidad) {

    @JsonIgnore
    @AssertTrue(message = "El precio de descuento no puede ser mayor al precio regular.")
    public boolean isPrecioDescuentoValido() {
        if (precioDescuento == null || precio == null) {
            return true;
        }
        return precioDescuento.compareTo(precio) <= 0;
    }

    public static ProductoDto fromEntity(Producto producto) {
        if (producto == null) {
            return null;
        }

        Integer idCat = null;
        Integer idSubCat = null;

        if (producto.getSubCategoria() != null) {
            idSubCat = producto.getSubCategoria().getId();
            if (producto.getSubCategoria().getCategoria() != null) {
                idCat = producto.getSubCategoria().getCategoria().getId();
            }
        }

        return new ProductoDto(
                producto.getId(),
                idCat,
                idSubCat,
                producto.getNombre(),
                producto.getPrecio(),
                producto.getPrecioDescuento(),
                producto.getDescripcion(),
                producto.getImgUrl(),
                producto.getCantidad());
    }

    public Producto toEntity() {
        Producto producto = new Producto();
        producto.setId(this.id);

        if (this.idSubCategoria != null || this.idCategoria != null) {
            SubCategoria subCat = new SubCategoria();
            subCat.setId(this.idSubCategoria);

            if (this.idCategoria != null) {
                Categoria cat = new Categoria();
                cat.setId(this.idCategoria);
                subCat.setCategoria(cat);
            }

            producto.setSubCategoria(subCat);
        }

        producto.setNombre(this.nombre);
        producto.setPrecio(this.precio);
        producto.setPrecioDescuento(this.precioDescuento);
        producto.setDescripcion(this.descripcion);
        producto.setImgUrl(this.imgUrl);
        producto.setCantidad(this.cantidad);

        return producto;
    }
}
