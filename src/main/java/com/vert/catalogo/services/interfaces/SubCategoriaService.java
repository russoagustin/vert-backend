package com.vert.catalogo.services.interfaces;

import java.util.List;

import com.vert.catalogo.dto.SubCategoriaDto;
import com.vert.catalogo.dto.SubCategoriaOrdenDto;

public interface SubCategoriaService {

    List<SubCategoriaDto> listarSubCategorias();

    List<SubCategoriaDto> listarPorCategoria(Integer idCategoria);

    Integer crearSubCategoria(SubCategoriaDto subCat);

    SubCategoriaDto buscarPorId(Integer id, Integer idCategoria);

    SubCategoriaDto buscarSubCategoria(Integer idCategoria, String nombre);

    void borrarSubCategoria(Integer id, Integer idCategoria);

    void modificarSubCategoria(Integer id, SubCategoriaDto subCat);

    void cambiarOrdenSubCategoria(Integer idCategoria, Integer idSubCategoria, Integer orden);

    void cambiarOrdenSubCategorias(Integer idCategoria, List<SubCategoriaOrdenDto> subcategorias);
}
