package com.vert.catalogo.services.interfaces;

import com.vert.catalogo.dto.CategoriaDto;

import java.util.List;

public interface CategoriaService {

    List<CategoriaDto> listarCategorias();

    Integer crearCategoria(CategoriaDto cat);

    CategoriaDto buscarPorId(Integer id);

    CategoriaDto buscarCategoria(String nombre);

    void borrarCategoria(Integer id);

    void modificarCategoria(Integer id, CategoriaDto cat);

    void cambiarOrdenCategoria(Integer id, Integer orden);
}
