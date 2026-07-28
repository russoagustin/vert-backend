package com.vert.catalogo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vert.catalogo.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	@Query(value = "CALL nueva_categoria(:p_nombre)", nativeQuery = true)
	Integer nuevaCategoria(@Param("p_nombre") String nombre);

	java.util.Optional<Categoria> findByNombre(String nombre);
}
