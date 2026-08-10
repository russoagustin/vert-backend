package com.vert.catalogo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.vert.catalogo.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	@Procedure(procedureName = "nueva_categoria", outputParameterName = "p_idCategoria")
	Integer nuevaCategoria(@Param("p_nombre") String nombre);

	@Procedure(procedureName = "modificar_categoria")
	void modificarCategoria(@Param("p_idCategoria") Integer id, @Param("p_nombre") String nombre);

	@Procedure(procedureName = "borrar_categoria")
	void borrarCategoria(@Param("p_idCategoria") Integer id);

	java.util.Optional<Categoria> findByNombre(String nombre);
}
