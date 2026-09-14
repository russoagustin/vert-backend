package com.vert.catalogo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vert.catalogo.dto.ProductoDto;
import com.vert.catalogo.services.interfaces.ProductoService;

@ExtendWith(MockitoExtension.class)
class ProductoPaginationIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private ProductoDto productoEjemplo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(productoService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        productoEjemplo = new ProductoDto(
                1,
                1,
                10,
                "Producto Test",
                new BigDecimal("1000.00"),
                new BigDecimal("900.00"),
                "Descripción producto test",
                "https://cdn.vert.com/test.png",
                20);
    }

    @Test
    @DisplayName("GET /api/productos sin parámetros aplica tamaño por defecto de 14 y orden ascendente por id")
    void listarProductos_SinParametros_AplicaPaginacionPorDefecto14ElementosYOrdenAscendentePorId() throws Exception {
        Pageable expectedPageable = PageRequest.of(0, 14, Sort.by("id").ascending());
        Page<ProductoDto> pageResult = new PageImpl<>(List.of(productoEjemplo), expectedPageable, 1);

        when(productoService.listarProductos(any(), any(), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].nombre", is("Producto Test")))
                .andExpect(jsonPath("$.size", is(14)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(true)))
                .andExpect(jsonPath("$.empty", is(false)));

        verify(productoService).listarProductos(eq(null), eq(null), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(14, captured.getPageSize(), "El tamaño de página por defecto debe ser 14");
        assertEquals(0, captured.getPageNumber(), "El número de página inicial debe ser 0");
        assertNotNull(captured.getSort().getOrderFor("id"), "Debe ordenar por 'id'");
        assertEquals(Sort.Direction.ASC, captured.getSort().getOrderFor("id").getDirection(), "El orden por defecto debe ser ASC");
    }

    @Test
    @DisplayName("GET /api/productos con query params personalizados pasa página, tamaño y orden correctos al servicio")
    void listarProductos_ConParametrosDePaginacionYOrdenPersonalizados_PasaParametrosCorrectos() throws Exception {
        Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "precio"));
        Page<ProductoDto> pageResult = new PageImpl<>(List.of(productoEjemplo), expectedPageable, 15);

        when(productoService.listarProductos(any(), any(), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos")
                .param("page", "1")
                .param("size", "5")
                .param("sort", "precio,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(3)));

        verify(productoService).listarProductos(eq(null), eq(null), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(5, captured.getPageSize());
        assertEquals(1, captured.getPageNumber());
        assertNotNull(captured.getSort().getOrderFor("precio"));
        assertEquals(Sort.Direction.DESC, captured.getSort().getOrderFor("precio").getDirection());
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{idCategoria} sin parámetros aplica 14 elementos por página")
    void listarPorCategoria_SinParametros_AplicaPaginacionPorDefecto14Elementos() throws Exception {
        Pageable expectedPageable = PageRequest.of(0, 14, Sort.by("id").ascending());
        Page<ProductoDto> pageResult = new PageImpl<>(List.of(productoEjemplo), expectedPageable, 1);

        when(productoService.listarPorCategoria(eq(1), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", is(14)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(productoService).listarPorCategoria(eq(1), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(14, captured.getPageSize(), "El tamaño de página debe ser 14");
        assertEquals(0, captured.getPageNumber());
        assertEquals(Sort.Direction.ASC, captured.getSort().getOrderFor("id").getDirection());
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{idCategoria} con parámetros personalizados")
    void listarPorCategoria_ConPaginacionPersonalizada() throws Exception {
        Pageable expectedPageable = PageRequest.of(2, 7);
        Page<ProductoDto> pageResult = new PageImpl<>(List.of(productoEjemplo), expectedPageable, 20);

        when(productoService.listarPorCategoria(eq(2), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos/categoria/2")
                .param("page", "2")
                .param("size", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(7)))
                .andExpect(jsonPath("$.number", is(2)));

        verify(productoService).listarPorCategoria(eq(2), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(7, captured.getPageSize());
        assertEquals(2, captured.getPageNumber());
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{idCategoria}/subcategoria/{idSubCategoria} aplica 14 elementos por defecto")
    void listarPorCategoriaYSubCategoria_AplicaPaginacionPorDefecto14Elementos() throws Exception {
        Pageable expectedPageable = PageRequest.of(0, 14, Sort.by("id").ascending());
        Page<ProductoDto> pageResult = new PageImpl<>(List.of(productoEjemplo), expectedPageable, 1);

        when(productoService.listarPorCategoriaYSubCategoria(eq(1), eq(10), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos/categoria/1/subcategoria/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", is(14)))
                .andExpect(jsonPath("$.number", is(0)));

        verify(productoService).listarPorCategoriaYSubCategoria(eq(1), eq(10), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(14, captured.getPageSize());
        assertEquals(0, captured.getPageNumber());
        assertEquals(Sort.Direction.ASC, captured.getSort().getOrderFor("id").getDirection());
    }

    @Test
    @DisplayName("GET /api/productos/categoria/{idCategoria}/subcategoria/{idSubCategoria} con parámetros personalizados")
    void listarPorCategoriaYSubCategoria_ConPaginacionPersonalizada() throws Exception {
        Pageable expectedPageable = PageRequest.of(3, 14);
        Page<ProductoDto> pageResult = new PageImpl<>(Collections.emptyList(), expectedPageable, 0);

        when(productoService.listarPorCategoriaYSubCategoria(eq(1), eq(10), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/productos/categoria/1/subcategoria/10")
                .param("page", "3")
                .param("size", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty", is(true)))
                .andExpect(jsonPath("$.totalElements", is(0)));

        verify(productoService).listarPorCategoriaYSubCategoria(eq(1), eq(10), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(14, captured.getPageSize());
        assertEquals(3, captured.getPageNumber());
    }

    @Test
    @DisplayName("GET /api/productos cuando no hay elementos retorna lista vacía y metadatos acordes")
    void listarProductos_PaginaVacia_RetornaContenidoVacioYMetadatosCorrectos() throws Exception {
        Pageable pageable = PageRequest.of(0, 14);
        Page<ProductoDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productoService.listarProductos(any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty", is(true)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.size", is(14)))
                .andExpect(jsonPath("$.number", is(0)));
    }
}
