package com.vert.catalogo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import com.vert.catalogo.dto.ProductoDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.entities.Producto;
import com.vert.catalogo.entities.SubCategoria;
import com.vert.catalogo.exceptions.NotFoundException;
import com.vert.catalogo.exceptions.ValidationException;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.repositories.ProductoRepository;
import com.vert.catalogo.repositories.SubCategoriaRepository;
import com.vert.catalogo.services.impl.DefaultProductoService;
import com.vert.catalogo.services.interfaces.StorageService;

@ExtendWith(MockitoExtension.class)
class DefaultProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SubCategoriaRepository subCategoriaRepository;

    @Mock
    private StorageService storageService;

    private DefaultProductoService productoService;

    private Categoria categoria;
    private SubCategoria subCategoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        productoService = new DefaultProductoService(
                productoRepository,
                categoriaRepository,
                subCategoriaRepository,
                storageService);

        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Bebidas");
        categoria.setOrden(1);

        subCategoria = new SubCategoria();
        subCategoria.setId(10);
        subCategoria.setCategoria(categoria);
        subCategoria.setNombre("Gaseosas");
        subCategoria.setOrden(1);

        producto = new Producto();
        producto.setId(100);
        producto.setSubCategoria(subCategoria);
        producto.setNombre("Coca Cola 1.5L");
        producto.setPrecio(new BigDecimal("1500.00"));
        producto.setPrecioDescuento(new BigDecimal("1200.00"));
        producto.setDescripcion("Bebida cola refrescante");
        producto.setImgUrl("https://pub-r2.vert.com/cocacola.png");
        producto.setCantidad(50);
    }

    @Test
    void crearProducto_Exitoso() {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "cocacola.png", "image/png", "imagen-bytes".getBytes());

        ProductoDto dto = new ProductoDto(
                null, 1, 10, "Coca Cola 1.5L",
                new BigDecimal("1500.00"), new BigDecimal("1200.00"),
                "Bebida cola", null, 50);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(subCategoria));
        when(storageService.uploadFile(imagen)).thenReturn("https://pub-r2.vert.com/cocacola-uuid.png");
        when(productoRepository.nuevoProducto(
                eq(1), eq(10), eq("Coca Cola 1.5L"),
                eq(new BigDecimal("1500.00")), eq(new BigDecimal("1200.00")),
                eq("Bebida cola"), eq("https://pub-r2.vert.com/cocacola-uuid.png"), eq(50)))
                .thenReturn(100);

        Integer newId = productoService.crearProducto(dto, imagen);

        assertEquals(100, newId);
        verify(storageService).uploadFile(imagen);
        verify(productoRepository).nuevoProducto(
                1, 10, "Coca Cola 1.5L", new BigDecimal("1500.00"), new BigDecimal("1200.00"),
                "Bebida cola", "https://pub-r2.vert.com/cocacola-uuid.png", 50);
    }

    @Test
    void crearProducto_SinImagen_LanzaValidationException() {
        ProductoDto dto = new ProductoDto(
                null, 1, 10, "Coca Cola 1.5L",
                new BigDecimal("1500.00"), null, null, null, 10);

        assertThrows(ValidationException.class, () -> productoService.crearProducto(dto, null));
        verify(storageService, never()).uploadFile(any());
        verify(productoRepository, never()).nuevoProducto(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void crearProducto_ImagenVacia_LanzaValidationException() {
        MockMultipartFile imagenVacia = new MockMultipartFile(
                "imagen", "empty.png", "image/png", new byte[0]);

        ProductoDto dto = new ProductoDto(
                null, 1, 10, "Coca Cola 1.5L",
                new BigDecimal("1500.00"), null, null, null, 10);

        assertThrows(ValidationException.class, () -> productoService.crearProducto(dto, imagenVacia));
        verify(storageService, never()).uploadFile(any());
    }

    @Test
    void crearProducto_CategoriaNoExiste_LanzaNotFoundException() {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.png", "image/png", "test".getBytes());
        ProductoDto dto = new ProductoDto(
                null, 999, 10, "Producto X",
                new BigDecimal("100.00"), null, null, null, 5);

        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.crearProducto(dto, imagen));
        verify(storageService, never()).uploadFile(any());
    }

    @Test
    void crearProducto_SubCategoriaNoExiste_LanzaNotFoundException() {
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.png", "image/png", "test".getBytes());
        ProductoDto dto = new ProductoDto(
                null, 1, 999, "Producto X",
                new BigDecimal("100.00"), null, null, null, 5);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(999, 1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.crearProducto(dto, imagen));
        verify(storageService, never()).uploadFile(any());
    }

    @Test
    void productoDto_PrecioDescuentoMayorQuePrecio_FallaValidacion() {
        ProductoDto dto = new ProductoDto(
                null, 1, 10, "Producto Caro",
                new BigDecimal("100.00"), new BigDecimal("150.00"), null, null, 5);

        jakarta.validation.Validator validator = jakarta.validation.Validation
                .buildDefaultValidatorFactory()
                .getValidator();

        var violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("El precio de descuento no puede ser mayor al precio regular.",
                violations.iterator().next().getMessage());
    }

    @Test
    void modificarProducto_ConNuevaImagen_ActualizaImagen() {
        MockMultipartFile nuevaImagen = new MockMultipartFile(
                "imagen", "nueva-foto.png", "image/png", "bytes-nuevos".getBytes());

        ProductoDto dto = new ProductoDto(
                100, 1, 10, "Coca Cola 2L",
                new BigDecimal("2000.00"), new BigDecimal("1800.00"),
                "Tamaño familiar", null, 30);

        when(productoRepository.findById(100)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(subCategoria));
        when(storageService.uploadFile(nuevaImagen)).thenReturn("https://pub-r2.vert.com/nueva-foto.png");

        productoService.modificarProducto(100, dto, nuevaImagen);

        verify(storageService).uploadFile(nuevaImagen);
        verify(productoRepository).modificarProducto(
                100, 1, 10, "Coca Cola 2L", new BigDecimal("2000.00"), new BigDecimal("1800.00"),
                "Tamaño familiar", "https://pub-r2.vert.com/nueva-foto.png", 30);
    }

    @Test
    void modificarProducto_SinImagen_MantieneImagenPrevia() {
        ProductoDto dto = new ProductoDto(
                100, 1, 10, "Coca Cola 1.5L Actualizada",
                new BigDecimal("1600.00"), null, "Nueva descripción", null, 40);

        when(productoRepository.findById(100)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(subCategoria));

        productoService.modificarProducto(100, dto, null);

        verify(storageService, never()).uploadFile(any());
        verify(productoRepository).modificarProducto(
                100, 1, 10, "Coca Cola 1.5L Actualizada", new BigDecimal("1600.00"), null,
                "Nueva descripción", "https://pub-r2.vert.com/cocacola.png", 40);
    }

    @Test
    void modificarProducto_ProductoNoExiste_LanzaNotFoundException() {
        ProductoDto dto = new ProductoDto(
                999, 1, 10, "No existe",
                new BigDecimal("100.00"), null, null, null, 1);

        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.modificarProducto(999, dto, null));
    }

    @Test
    void borrarProducto_Exitoso() {
        when(productoRepository.findById(100)).thenReturn(Optional.of(producto));

        productoService.borrarProducto(100);

        verify(productoRepository).delete(producto);
    }

    @Test
    void borrarProducto_NoExiste_LanzaNotFoundException() {
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.borrarProducto(999));
    }

    @Test
    void buscarPorId_Exitoso() {
        when(productoRepository.findById(100)).thenReturn(Optional.of(producto));

        ProductoDto resultado = productoService.buscarPorId(100);

        assertNotNull(resultado);
        assertEquals(100, resultado.id());
        assertEquals("Coca Cola 1.5L", resultado.nombre());
        assertEquals(1, resultado.idCategoria());
        assertEquals(10, resultado.idSubCategoria());
    }

    @Test
    void buscarPorNombre_Exitoso() {
        when(productoRepository.findByNombre("Coca Cola 1.5L")).thenReturn(Optional.of(producto));

        ProductoDto resultado = productoService.buscarPorNombre("Coca Cola 1.5L");

        assertNotNull(resultado);
        assertEquals(100, resultado.id());
        assertEquals("Coca Cola 1.5L", resultado.nombre());
    }

    @Test
    void listarProductos_SinFiltros() {
        Pageable pageable = PageRequest.of(0, 14);
        Page<Producto> page = new PageImpl<>(List.of(producto), pageable, 1);
        when(productoRepository.findAll(pageable)).thenReturn(page);

        Page<ProductoDto> resultado = productoService.listarProductos(null, null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("Coca Cola 1.5L", resultado.getContent().get(0).nombre());
        verify(productoRepository).findAll(pageable);
    }

    @Test
    void listarProductos_FiltradoPorCategoria() {
        Pageable pageable = PageRequest.of(0, 14);
        Page<Producto> page = new PageImpl<>(List.of(producto), pageable, 1);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.findAllByCategoriaId(1, pageable)).thenReturn(page);

        Page<ProductoDto> resultado = productoService.listarProductos(1, null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("Coca Cola 1.5L", resultado.getContent().get(0).nombre());
        verify(productoRepository).findAllByCategoriaId(1, pageable);
    }

    @Test
    void listarProductos_FiltradoPorCategoriaYSubCategoria() {
        Pageable pageable = PageRequest.of(0, 14);
        Page<Producto> page = new PageImpl<>(List.of(producto), pageable, 1);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(subCategoria));
        when(productoRepository.findAllBySubCategoriaIdAndCategoriaId(10, 1, pageable)).thenReturn(page);

        Page<ProductoDto> resultado = productoService.listarProductos(1, 10, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        assertEquals("Coca Cola 1.5L", resultado.getContent().get(0).nombre());
        verify(productoRepository).findAllBySubCategoriaIdAndCategoriaId(10, 1, pageable);
    }

    @Test
    void listarPorCategoria_CategoriaNoExiste_LanzaNotFoundException() {
        Pageable pageable = PageRequest.of(0, 14);
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.listarPorCategoria(999, pageable));
        verify(productoRepository, never()).findAllByCategoriaId(any(), any());
    }

    @Test
    void listarPorCategoriaYSubCategoria_SubCategoriaNoExiste_LanzaNotFoundException() {
        Pageable pageable = PageRequest.of(0, 14);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findByIdAndCategoriaId(999, 1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productoService.listarPorCategoriaYSubCategoria(1, 999, pageable));
        verify(productoRepository, never()).findAllBySubCategoriaIdAndCategoriaId(any(), any(), any());
    }
}
