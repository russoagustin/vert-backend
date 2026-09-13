package com.vert.catalogo.services.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vert.catalogo.dto.CategoriaDto;
import com.vert.catalogo.dto.CategoriaOrdenDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.services.interfaces.CategoriaService;

import com.vert.catalogo.exceptions.NotFoundException;
import com.vert.catalogo.exceptions.ValidationException;

@Service
public class DefaultCategoriaService implements CategoriaService {

    private final CategoriaRepository repository;

    public DefaultCategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategoriaDto> listarCategorias() {
        return this.repository.findAllByOrderByOrdenAsc().stream().map(CategoriaDto::fromEntity).toList();
    }

    @Override
    public Integer crearCategoria(CategoriaDto cat) {
        return this.repository.nuevaCategoria(cat.nombre());
    }

    @Override
    public CategoriaDto buscarPorId(Integer id) {
        Optional<Categoria> cat = this.repository.findById(id);

        if (!cat.isPresent()) {
            throw new NotFoundException("Categoria no encontrada");
        }

        return CategoriaDto.fromEntity(cat.get());
    }

    @Override
    public CategoriaDto buscarCategoria(String nombre) {
        Optional<Categoria> cat = this.repository.findByNombre(nombre);

        if (!cat.isPresent()) {
            throw new NotFoundException("Categoria no encontrada");
        }

        return CategoriaDto.fromEntity(cat.get());
    }

    @Override
    public void borrarCategoria(Integer id) {
        Categoria cat = this.repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada"));

        this.repository.delete(cat);
    }

    @Override
    public void modificarCategoria(Integer id, CategoriaDto cat) {
        this.repository.findById(id).orElseThrow(() -> new NotFoundException("Categoria no encontrada"));
        this.repository.modificarCategoria(id, cat.nombre());
    }

    @Override
    public void cambiarOrdenCategoria(Integer id, Integer orden) {
        this.repository.findById(id).orElseThrow(() -> new NotFoundException("Categoria no encontrada"));
        this.repository.cambiarOrdenCategoria(id, orden);
    }

    @Override
    @Transactional
    public void cambiarOrdenCategorias(List<CategoriaOrdenDto> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            throw new ValidationException("La lista de categorías no puede ser nula ni estar vacía.");
        }

        this.normalizarOrdenes(categorias);

        Set<Integer> ordenes = new HashSet<>();
        Set<Integer> ids = new HashSet<>();

        for (CategoriaOrdenDto item : categorias) {
            if (item == null) {
                throw new ValidationException("El elemento de la lista no puede ser nulo.");
            }
            if (item.getId() == null) {
                throw new ValidationException("El ID de la categoría no puede ser nulo.");
            }
            if (item.getOrden() == null) {
                throw new ValidationException("El orden de la categoría no puede ser nulo.");
            }
            if (!ids.add(item.getId())) {
                throw new ValidationException("No se permiten IDs de categoría duplicados: " + item.getId());
            }
            if (!ordenes.add(item.getOrden())) {
                throw new ValidationException("No se permiten órdenes repetidos: orden " + item.getOrden());
            }
        }

        long totalCategorias = this.repository.count();
        if (categorias.size() != totalCategorias) {
            throw new ValidationException(
                    "Debe enviar el orden de todas las categorías (" + totalCategorias + " categorías registradas).");
        }

        for (CategoriaOrdenDto item : categorias) {
            Categoria cat = this.repository.findById(item.getId())
                    .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + item.getId()));
            cat.setOrden(item.getOrden());
            this.repository.save(cat);
        }
    }

    private void normalizarOrdenes(List<CategoriaOrdenDto> categorias) {

        categorias.sort(Comparator.comparing(CategoriaOrdenDto::getOrden)); // ordeno la categoria
        int len = categorias.size();
        for (int i = 0; i < len; i++) {
            if (categorias.get(i).getOrden() != (i + 1)) {
                categorias.get(i).setOrden(i + 1);
            }
        }
    }

}
