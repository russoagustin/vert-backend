package com.vert.catalogo.services.interfaces;

import com.vert.catalogo.entities.Categoria;

public interface CategoriaService {

    Integer crearCategoria(Categoria cat);
    Categoria buscarPorId(Integer id);
    Categoria buscarCategoria(String nombre);
    void borrarCategoria(Categoria cat);
    void modificarCategoria(Categoria cat);
}
