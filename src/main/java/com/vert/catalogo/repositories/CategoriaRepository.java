package com.vert.catalogo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.vert.catalogo.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	/**
	 * @param nombre Nombre de la categoría
	 *               <p>
	 *               Procedimiento almacenado que permite crear una nueva categoría
	 *               El orden por defecto será el último de la tabla.
	 *               </p>
	 */
	@Procedure(procedureName = "nueva_categoria", outputParameterName = "p_idCategoria")
	Integer nuevaCategoria(@Param("p_nombre") String nombre);

	/**
	 * @param id     Id de la categoría a modificar
	 * @param nombre Nombre de la categoría
	 *               <p>
	 *               Procedimiento almacenado que permite únicamente modificar el
	 *               nombre de
	 *               una
	 *               categoría
	 *               </p>
	 */
	@Procedure(procedureName = "modificar_categoria")
	void modificarCategoria(@Param("p_idCategoria") Integer id, @Param("p_nombre") String nombre);

	/**
	 * @param id Id de la categoría a eliminar
	 *           <p>
	 *           Procedimiento almacenado que permite eliminar una categoría
	 *           </p>
	 */
	@Procedure(procedureName = "borrar_categoria")
	void borrarCategoria(@Param("p_idCategoria") Integer id);

	/**
	 * @param id    Id de la categoría a modificar
	 * @param orden Orden de la categoría
	 *              <p>
	 *              Procedimiento almacenado que permite modificar el orden de una
	 *              categoría y actualiza el del resto.
	 *              </p>
	 */
	@Procedure(procedureName = "cambiar_orden_categoria")
	void cambiarOrdenCategoria(@Param("p_idCategoria") Integer id, @Param("p_orden") Integer orden);

	Optional<Categoria> findByNombre(String nombre);

	List<Categoria> findAllByOrderByOrdenAsc();
}
