package com.vert.catalogo.services.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vert.catalogo.dto.ProductoDto;

public interface ProductoService {

    List<ProductoDto> listarProductos(Integer idCategoria, Integer idSubCategoria);

    List<ProductoDto> listarPorCategoria(Integer idCategoria);

    List<ProductoDto> listarPorCategoriaYSubCategoria(Integer idCategoria, Integer idSubCategoria);

    ProductoDto buscarPorId(Integer id);

    ProductoDto buscarPorNombre(String nombre);

    Integer crearProducto(ProductoDto productoDto, MultipartFile imagen);

    void modificarProducto(Integer id, ProductoDto productoDto, MultipartFile imagen);

    void borrarProducto(Integer id);
}
