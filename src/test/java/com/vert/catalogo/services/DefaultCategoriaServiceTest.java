package com.vert.catalogo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vert.catalogo.dto.CategoriaOrdenDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.exceptions.ValidationException;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.services.impl.DefaultCategoriaService;

@ExtendWith(MockitoExtension.class)
class DefaultCategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private DefaultCategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new DefaultCategoriaService(categoriaRepository);
    }

    @Test
    void cambiarOrdenCategorias_Exitoso() {
        Categoria cat1 = new Categoria();
        cat1.setId(1);
        cat1.setNombre("Bebidas");
        cat1.setOrden(1);

        Categoria cat2 = new Categoria();
        cat2.setId(2);
        cat2.setNombre("Snacks");
        cat2.setOrden(2);

        when(categoriaRepository.count()).thenReturn(2L);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(cat1));
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(cat2));

        List<CategoriaOrdenDto> reorden = List.of(
                new CategoriaOrdenDto(1, 2),
                new CategoriaOrdenDto(2, 1)
        );

        categoriaService.cambiarOrdenCategorias(reorden);

        assertEquals(2, cat1.getOrden());
        assertEquals(1, cat2.getOrden());
        verify(categoriaRepository, times(2)).save(any(Categoria.class));
    }

    @Test
    void cambiarOrdenCategorias_OrdenDuplicado_LanzaValidationException() {
        List<CategoriaOrdenDto> reorden = List.of(
                new CategoriaOrdenDto(1, 1),
                new CategoriaOrdenDto(2, 1)
        );

        assertThrows(ValidationException.class, () -> categoriaService.cambiarOrdenCategorias(reorden));
    }

    @Test
    void cambiarOrdenCategorias_CantidadIncompleta_LanzaValidationException() {
        when(categoriaRepository.count()).thenReturn(3L);

        List<CategoriaOrdenDto> reorden = List.of(
                new CategoriaOrdenDto(1, 1),
                new CategoriaOrdenDto(2, 2)
        );

        assertThrows(ValidationException.class, () -> categoriaService.cambiarOrdenCategorias(reorden));
    }

    @Test
    void listarCategorias_DevuelveOrdenadoPorOrden() {
        Categoria cat1 = new Categoria();
        cat1.setId(1);
        cat1.setNombre("Bebidas");
        cat1.setOrden(1);

        Categoria cat2 = new Categoria();
        cat2.setId(2);
        cat2.setNombre("Snacks");
        cat2.setOrden(2);

        when(categoriaRepository.findAllByOrderByOrdenAsc()).thenReturn(List.of(cat1, cat2));

        var resultado = categoriaService.listarCategorias();

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).orden());
        assertEquals(2, resultado.get(1).orden());
        verify(categoriaRepository).findAllByOrderByOrdenAsc();
    }
}
