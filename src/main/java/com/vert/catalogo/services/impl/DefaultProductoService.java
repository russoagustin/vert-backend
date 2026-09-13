package com.vert.catalogo.services.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vert.catalogo.dto.ProductoDto;
import com.vert.catalogo.entities.Producto;
import com.vert.catalogo.exceptions.NotFoundException;
import com.vert.catalogo.exceptions.ValidationException;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.repositories.ProductoRepository;
import com.vert.catalogo.repositories.SubCategoriaRepository;
import com.vert.catalogo.services.interfaces.ProductoService;
import com.vert.catalogo.services.interfaces.StorageService;

@Service
public class DefaultProductoService implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final StorageService storageService;

    public DefaultProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            SubCategoriaRepository subCategoriaRepository,
            StorageService storageService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subCategoriaRepository = subCategoriaRepository;
        this.storageService = storageService;
    }

    @Override
    public List<ProductoDto> listarProductos(Integer idCategoria, Integer idSubCategoria) {
        if (idCategoria != null && idSubCategoria != null) {
            return this.listarPorCategoriaYSubCategoria(idCategoria, idSubCategoria);
        } else if (idCategoria != null) {
            return this.listarPorCategoria(idCategoria);
        }

        return this.productoRepository.findAll()
                .stream()
                .map(ProductoDto::fromEntity)
                .toList();
    }

    @Override
    public List<ProductoDto> listarPorCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }

        this.categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + idCategoria));

        return this.productoRepository.findAllByCategoriaId(idCategoria)
                .stream()
                .map(ProductoDto::fromEntity)
                .toList();
    }

    @Override
    public List<ProductoDto> listarPorCategoriaYSubCategoria(Integer idCategoria, Integer idSubCategoria) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }
        if (idSubCategoria == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }

        this.categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + idCategoria));

        this.subCategoriaRepository.findByIdAndCategoriaId(idSubCategoria, idCategoria)
                .orElseThrow(() -> new NotFoundException(
                        "SubCategoria no encontrada con id: " + idSubCategoria + " para la categoría: " + idCategoria));

        return this.productoRepository.findAllBySubCategoriaIdAndCategoriaId(idSubCategoria, idCategoria)
                .stream()
                .map(ProductoDto::fromEntity)
                .toList();
    }

    @Override
    public ProductoDto buscarPorId(Integer id) {
        if (id == null) {
            throw new ValidationException("El ID del producto no puede ser nulo.");
        }

        Producto producto = this.productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));

        return ProductoDto.fromEntity(producto);
    }

    @Override
    public ProductoDto buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("El nombre no puede ser nulo ni estar vacío.");
        }

        Producto producto = this.productoRepository.findByNombre(nombre.trim())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con nombre: " + nombre.trim()));

        return ProductoDto.fromEntity(producto);
    }

    @Override
    public Integer crearProducto(ProductoDto productoDto, MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new ValidationException("La imagen del producto es obligatoria.");
        }
        if (productoDto == null) {
            throw new ValidationException("Los datos del producto no pueden ser nulos.");
        }
        if (productoDto.idCategoria() == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }
        if (productoDto.idSubCategoria() == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }
        if (productoDto.nombre() == null || productoDto.nombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del producto no puede ser nulo ni estar vacío.");
        }
        if (productoDto.precio() == null || productoDto.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El precio del producto debe ser mayor o igual a 0.");
        }
        if (productoDto.precioDescuento() != null &&
                (productoDto.precioDescuento().compareTo(BigDecimal.ZERO) < 0
                        || productoDto.precioDescuento().compareTo(productoDto.precio()) > 0)) {
            throw new ValidationException("El precio de descuento no puede ser negativo ni mayor al precio regular.");
        }

        this.categoriaRepository.findById(productoDto.idCategoria())
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + productoDto.idCategoria()));

        this.subCategoriaRepository.findByIdAndCategoriaId(productoDto.idSubCategoria(), productoDto.idCategoria())
                .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada con id: "
                        + productoDto.idSubCategoria() + " para la categoría: " + productoDto.idCategoria()));

        String imgUrl = this.storageService.uploadFile(imagen);

        return this.productoRepository.nuevoProducto(
                productoDto.idCategoria(),
                productoDto.idSubCategoria(),
                productoDto.nombre().trim(),
                productoDto.precio(),
                productoDto.precioDescuento(),
                productoDto.descripcion(),
                imgUrl,
                productoDto.cantidad());
    }

    @Override
    public void modificarProducto(Integer id, ProductoDto productoDto, MultipartFile imagen) {
        if (id == null) {
            throw new ValidationException("El ID del producto no puede ser nulo.");
        }
        if (productoDto == null) {
            throw new ValidationException("Los datos del producto no pueden ser nulos.");
        }

        Producto existing = this.productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));

        Integer resolvedCatId = productoDto.idCategoria();
        Integer resolvedSubCatId = productoDto.idSubCategoria();

        if (resolvedCatId == null && existing.getSubCategoria() != null && existing.getSubCategoria().getCategoria() != null) {
            resolvedCatId = existing.getSubCategoria().getCategoria().getId();
        }
        if (resolvedSubCatId == null && existing.getSubCategoria() != null) {
            resolvedSubCatId = existing.getSubCategoria().getId();
        }

        if (resolvedCatId == null || resolvedSubCatId == null) {
            throw new ValidationException("El ID de la categoría y subcategoría son obligatorios.");
        }

        final Integer finalCatId = resolvedCatId;
        final Integer finalSubCatId = resolvedSubCatId;

        this.categoriaRepository.findById(finalCatId)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + finalCatId));

        this.subCategoriaRepository.findByIdAndCategoriaId(finalSubCatId, finalCatId)
                .orElseThrow(() -> new NotFoundException(
                        "SubCategoria no encontrada con id: " + finalSubCatId + " para la categoría: " + finalCatId));

        String imgUrl;
        if (imagen != null && !imagen.isEmpty()) {
            imgUrl = this.storageService.uploadFile(imagen);
        } else {
            imgUrl = existing.getImgUrl();
        }

        String nombre = productoDto.nombre() != null ? productoDto.nombre().trim() : existing.getNombre();
        if (nombre == null || nombre.isEmpty()) {
            throw new ValidationException("El nombre del producto no puede ser nulo ni estar vacío.");
        }

        BigDecimal precio = productoDto.precio() != null ? productoDto.precio() : existing.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("El precio del producto debe ser mayor o igual a 0.");
        }

        BigDecimal precioDescuento = productoDto.precioDescuento();
        if (precioDescuento != null &&
                (precioDescuento.compareTo(BigDecimal.ZERO) < 0 || precioDescuento.compareTo(precio) > 0)) {
            throw new ValidationException("El precio de descuento no puede ser negativo ni mayor al precio regular.");
        }

        String descripcion = productoDto.descripcion();
        Integer cantidad = productoDto.cantidad();

        this.productoRepository.modificarProducto(
                id,
                finalCatId,
                finalSubCatId,
                nombre,
                precio,
                precioDescuento,
                descripcion,
                imgUrl,
                cantidad);
    }

    @Override
    public void borrarProducto(Integer id) {
        if (id == null) {
            throw new ValidationException("El ID del producto no puede ser nulo.");
        }

        Producto existing = this.productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));

        this.productoRepository.delete(existing);
    }
}
