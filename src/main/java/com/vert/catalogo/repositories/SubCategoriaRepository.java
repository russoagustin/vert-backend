package com.vert.catalogo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.vert.catalogo.entities.SubCategoria;
import com.vert.catalogo.entities.SubCategoriaId;

public interface SubCategoriaRepository extends JpaRepository<SubCategoria, SubCategoriaId> {

    /**
     * @param idCategoria ID de la categoría a la que pertenece
     * @param nombre      Nombre de la subcategoría
     *                    <p>
     *                    Procedimiento almacenado que permite crear una nueva subcategoría.
     *                    El orden por defecto será el último de la categoría especificada.
     *                    </p>
     */
    @Procedure(procedureName = "nueva_subcategoria", outputParameterName = "p_idSubCategoria")
    Integer nuevaSubCategoria(
            @Param("p_idCategoria") Integer idCategoria,
            @Param("p_nombre") String nombre);

    /**
     * @param idCategoria    ID de la categoría
     * @param idSubCategoria ID de la subcategoría a modificar
     * @param nombre         Nuevo nombre de la subcategoría
     *                       <p>
     *                       Procedimiento almacenado que permite únicamente modificar el nombre
     *                       de una subcategoría perteneciente a una categoría.
     *                       </p>
     */
    @Procedure(procedureName = "modificar_subcategoria")
    void modificarSubCategoria(
            @Param("p_idCategoria") Integer idCategoria,
            @Param("p_idSubCategoria") Integer idSubCategoria,
            @Param("p_nombre") String nombre);

    /**
     * @param idCategoria    ID de la categoría
     * @param idSubCategoria ID de la subcategoría a modificar
     * @param orden          Nuevo orden dentro de la categoría
     *                       <p>
     *                       Procedimiento almacenado que permite modificar el orden de una
     *                       subcategoría y actualiza el del resto de subcategorías de la misma categoría.
     *                       </p>
     */
    @Procedure(procedureName = "cambiar_orden_subcategoria")
    void cambiarOrdenSubCategoria(
            @Param("p_idCategoria") Integer idCategoria,
            @Param("p_idSubCategoria") Integer idSubCategoria,
            @Param("p_orden") Integer orden);

    List<SubCategoria> findAllByCategoriaIdOrderByOrdenAsc(Integer idCategoria);

    List<SubCategoria> findAllByOrderByCategoriaIdAscOrdenAsc();

    Optional<SubCategoria> findByIdAndCategoriaId(Integer id, Integer idCategoria);

    Optional<SubCategoria> findByCategoriaIdAndNombre(Integer idCategoria, String nombre);

    long countByCategoriaId(Integer idCategoria);
}
