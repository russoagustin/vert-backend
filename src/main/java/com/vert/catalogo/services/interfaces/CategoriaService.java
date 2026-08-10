package com.vert.catalogo.services.interfaces;

import com.vert.catalogo.entities.Categoria;

import java.util.List;

public interface CategoriaService {

    List<Categoria> listarCategorias();

    Integer crearCategoria(Categoria cat);

    Categoria buscarPorId(Integer id);

    Categoria buscarCategoria(String nombre);

    void borrarCategoria(Categoria cat);

    void modificarCategoria(Categoria cat);
}
