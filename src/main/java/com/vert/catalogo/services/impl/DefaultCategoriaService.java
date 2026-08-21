package com.vert.catalogo.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vert.catalogo.dto.CategoriaDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.services.interfaces.CategoriaService;

import com.vert.catalogo.exceptions.NotFoundException;

@Service
public class DefaultCategoriaService implements CategoriaService {

    private final CategoriaRepository repository;

    public DefaultCategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategoriaDto> listarCategorias() {
        return this.repository.findAll().stream().map(CategoriaDto::fromEntity).toList();
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

}
