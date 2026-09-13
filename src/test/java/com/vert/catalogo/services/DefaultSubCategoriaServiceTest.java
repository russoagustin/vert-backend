package com.vert.catalogo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vert.catalogo.dto.SubCategoriaDto;
import com.vert.catalogo.dto.SubCategoriaOrdenDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.entities.SubCategoria;
import com.vert.catalogo.exceptions.NotFoundException;
import com.vert.catalogo.exceptions.ValidationException;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.repositories.SubCategoriaRepository;
import com.vert.catalogo.services.impl.DefaultSubCategoriaService;

@ExtendWith(MockitoExtension.class)
class DefaultSubCategoriaServiceTest {

    @Mock
    private SubCategoriaRepository subCategoriaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private DefaultSubCategoriaService subCategoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        subCategoriaService = new DefaultSubCategoriaService(subCategoriaRepository, categoriaRepository);

        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Bebidas");
        categoria.setOrden(1);
    }

    @Test
    void cambiarOrdenSubCategorias_Exitoso() {
        SubCategoria sub1 = new SubCategoria();
        sub1.setId(10);
        sub1.setCategoria(categoria);
        sub1.setNombre("Gaseosas");
        sub1.setOrden(1);

        SubCategoria sub2 = new SubCategoria();
        sub2.setId(20);
        sub2.setCategoria(categoria);
        sub2.setNombre("Aguas");
        sub2.setOrden(2);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.countByCategoriaId(1)).thenReturn(2L);
        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(sub1));
        when(subCategoriaRepository.findByIdAndCategoriaId(20, 1)).thenReturn(Optional.of(sub2));

        List<SubCategoriaOrdenDto> reorden = new ArrayList<>(List.of(
                new SubCategoriaOrdenDto(10, 2),
                new SubCategoriaOrdenDto(20, 1)
        ));

        subCategoriaService.cambiarOrdenSubCategorias(1, reorden);

        assertEquals(2, sub1.getOrden());
        assertEquals(1, sub2.getOrden());
        verify(subCategoriaRepository, times(2)).save(any(SubCategoria.class));
    }

    @Test
    void cambiarOrdenSubCategorias_CategoriaNoExiste_LanzaNotFoundException() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        List<SubCategoriaOrdenDto> reorden = new ArrayList<>(List.of(
                new SubCategoriaOrdenDto(10, 1)
        ));

        assertThrows(NotFoundException.class, () -> subCategoriaService.cambiarOrdenSubCategorias(99, reorden));
    }

    @Test
    void cambiarOrdenSubCategorias_OrdenDuplicado_LanzaValidationException() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        List<SubCategoriaOrdenDto> reorden = new ArrayList<>(List.of(
                new SubCategoriaOrdenDto(10, 1),
                new SubCategoriaOrdenDto(20, 1)
        ));

        assertThrows(ValidationException.class, () -> subCategoriaService.cambiarOrdenSubCategorias(1, reorden));
    }

    @Test
    void cambiarOrdenSubCategorias_CantidadIncompleta_LanzaValidationException() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.countByCategoriaId(1)).thenReturn(3L);

        List<SubCategoriaOrdenDto> reorden = new ArrayList<>(List.of(
                new SubCategoriaOrdenDto(10, 1),
                new SubCategoriaOrdenDto(20, 2)
        ));

        assertThrows(ValidationException.class, () -> subCategoriaService.cambiarOrdenSubCategorias(1, reorden));
    }

    @Test
    void listarSubCategorias_DevuelveOrdenado() {
        SubCategoria sub1 = new SubCategoria();
        sub1.setId(10);
        sub1.setCategoria(categoria);
        sub1.setNombre("Gaseosas");
        sub1.setOrden(1);

        SubCategoria sub2 = new SubCategoria();
        sub2.setId(20);
        sub2.setCategoria(categoria);
        sub2.setNombre("Aguas");
        sub2.setOrden(2);

        when(subCategoriaRepository.findAllByOrderByCategoriaIdAscOrdenAsc()).thenReturn(List.of(sub1, sub2));

        var resultado = subCategoriaService.listarSubCategorias();

        assertEquals(2, resultado.size());
        assertEquals(10, resultado.get(0).id());
        assertEquals(20, resultado.get(1).id());
        verify(subCategoriaRepository).findAllByOrderByCategoriaIdAscOrdenAsc();
    }

    @Test
    void listarPorCategoria_DevuelveOrdenadoPorOrden() {
        SubCategoria sub1 = new SubCategoria();
        sub1.setId(10);
        sub1.setCategoria(categoria);
        sub1.setNombre("Gaseosas");
        sub1.setOrden(1);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.findAllByCategoriaIdOrderByOrdenAsc(1)).thenReturn(List.of(sub1));

        var resultado = subCategoriaService.listarPorCategoria(1);

        assertEquals(1, resultado.size());
        assertEquals("Gaseosas", resultado.get(0).nombre());
        verify(subCategoriaRepository).findAllByCategoriaIdOrderByOrdenAsc(1);
    }

    @Test
    void crearSubCategoria_Exitoso() {
        SubCategoriaDto dto = new SubCategoriaDto(null, 1, "Cervezas", null);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(subCategoriaRepository.nuevaSubCategoria(1, "Cervezas")).thenReturn(15);

        Integer newId = subCategoriaService.crearSubCategoria(dto);

        assertEquals(15, newId);
        verify(subCategoriaRepository).nuevaSubCategoria(1, "Cervezas");
    }

    @Test
    void buscarPorId_Exitoso() {
        SubCategoria sub = new SubCategoria();
        sub.setId(10);
        sub.setCategoria(categoria);
        sub.setNombre("Gaseosas");
        sub.setOrden(1);

        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(sub));

        SubCategoriaDto resultado = subCategoriaService.buscarPorId(10, 1);

        assertNotNull(resultado);
        assertEquals(10, resultado.id());
        assertEquals("Gaseosas", resultado.nombre());
    }

    @Test
    void modificarSubCategoria_Exitoso() {
        SubCategoria sub = new SubCategoria();
        sub.setId(10);
        sub.setCategoria(categoria);
        sub.setNombre("Gaseosas");
        sub.setOrden(1);

        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(sub));

        SubCategoriaDto dto = new SubCategoriaDto(10, 1, "Gaseosas Zero", 1);
        subCategoriaService.modificarSubCategoria(10, dto);

        verify(subCategoriaRepository).modificarSubCategoria(1, 10, "Gaseosas Zero");
    }

    @Test
    void borrarSubCategoria_Exitoso() {
        SubCategoria sub = new SubCategoria();
        sub.setId(10);
        sub.setCategoria(categoria);
        sub.setNombre("Gaseosas");
        sub.setOrden(1);

        when(subCategoriaRepository.findByIdAndCategoriaId(10, 1)).thenReturn(Optional.of(sub));

        subCategoriaService.borrarSubCategoria(10, 1);

        verify(subCategoriaRepository).delete(sub);
    }
}
