package com.vert.catalogo.repositories;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.vert.catalogo.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

        /**
         * Procedimiento almacenado que crea un nuevo producto asociado a una categoría
         * y subcategoría.
         */
        @Procedure(procedureName = "nuevo_producto", outputParameterName = "p_idProducto")
        Integer nuevoProducto(
                        @Param("p_idCategoria") Integer idCategoria,
                        @Param("p_idSubCategoria") Integer idSubCategoria,
                        @Param("p_nombre") String nombre,
                        @Param("p_precio") BigDecimal precio,
                        @Param("p_precioDescuento") BigDecimal precioDescuento,
                        @Param("p_descripcion") String descripcion,
                        @Param("p_imgUrl") String imgUrl,
                        @Param("p_cantidad") Integer cantidad);

        /**
         * Procedimiento almacenado que modifica los datos de un producto existente.
         */
        @Procedure(procedureName = "modificar_producto")
        void modificarProducto(
                        @Param("p_idProducto") Integer idProducto,
                        @Param("p_idCategoria") Integer idCategoria,
                        @Param("p_idSubCategoria") Integer idSubCategoria,
                        @Param("p_nombre") String nombre,
                        @Param("p_precio") BigDecimal precio,
                        @Param("p_precioDescuento") BigDecimal precioDescuento,
                        @Param("p_descripcion") String descripcion,
                        @Param("p_imgUrl") String imgUrl,
                        @Param("p_cantidad") Integer cantidad);

        Optional<Producto> findByNombre(String nombre);

        @Query(value = "SELECT p FROM Producto p WHERE p.subCategoria.categoria.id = :idCategoria", countQuery = "SELECT count(p) FROM Producto p WHERE p.subCategoria.categoria.id = :idCategoria")
        Page<Producto> findAllByCategoriaId(@Param("idCategoria") Integer idCategoria, Pageable pageable);

        @Query(value = "SELECT p FROM Producto p WHERE p.subCategoria.id = :idSubCategoria AND p.subCategoria.categoria.id = :idCategoria", countQuery = "SELECT count(p) FROM Producto p WHERE p.subCategoria.id = :idSubCategoria AND p.subCategoria.categoria.id = :idCategoria")
        Page<Producto> findAllBySubCategoriaIdAndCategoriaId(
                        @Param("idSubCategoria") Integer idSubCategoria,
                        @Param("idCategoria") Integer idCategoria,
                        Pageable pageable);

        @Query("SELECT p FROM Producto p WHERE p.id = :id AND p.subCategoria.categoria.id = :idCategoria")
        Optional<Producto> findByIdAndCategoriaId(
                        @Param("id") Integer id,
                        @Param("idCategoria") Integer idCategoria);
}
