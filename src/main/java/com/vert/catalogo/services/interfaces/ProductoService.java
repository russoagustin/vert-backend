package com.vert.catalogo.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.vert.catalogo.dto.ProductoDto;

public interface ProductoService {

    Page<ProductoDto> listarProductos(Integer idCategoria, Integer idSubCategoria, Pageable pageable);

    Page<ProductoDto> listarPorCategoria(Integer idCategoria, Pageable pageable);

    Page<ProductoDto> listarPorCategoriaYSubCategoria(Integer idCategoria, Integer idSubCategoria, Pageable pageable);

    ProductoDto buscarPorId(Integer id);

    ProductoDto buscarPorNombre(String nombre);

    Integer crearProducto(ProductoDto productoDto, MultipartFile imagen);

    void modificarProducto(Integer id, ProductoDto productoDto, MultipartFile imagen);

    void borrarProducto(Integer id);
}
