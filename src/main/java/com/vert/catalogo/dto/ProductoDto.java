package com.vert.catalogo.dto;

import java.math.BigDecimal;

import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.entities.Producto;
import com.vert.catalogo.entities.SubCategoria;

public record ProductoDto(
        Integer id,
        Integer idCategoria,
        Integer idSubCategoria,
        String nombre,
        BigDecimal precio,
        BigDecimal precioDescuento,
        String descripcion,
        String imgUrl,
        Integer cantidad) {

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
