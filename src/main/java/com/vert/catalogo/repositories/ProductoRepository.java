package com.vert.catalogo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import com.vert.catalogo.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	@Procedure(procedureName = "nuevo_producto", outputParameterName = "p_idProducto")
	Integer nuevoProducto(
        @Param("p_idCategoria") Integer idCategoria, 
        @Param("p_nombre") String nombre, 
        @Param("p_precio") BigDecimal precio, 
        @Param("p_precioDescuento") BigDecimal precioDescuento, 
        @Param("p_descripcion") String descripcion, 
        @Param("p_imgUrl") String imgUrl
    );

	@Procedure(procedureName = "modificar_producto")
	void modificarProducto(
        @Param("p_idProducto") Integer idProducto,
        @Param("p_idCategoria") Integer idCategoria, 
        @Param("p_nombre") String nombre, 
        @Param("p_precio") BigDecimal precio, 
        @Param("p_precioDescuento") BigDecimal precioDescuento, 
        @Param("p_descripcion") String descripcion, 
        @Param("p_imgUrl") String imgUrl
    );

    java.util.Optional<Producto> findByNombre(String nombre);
}
