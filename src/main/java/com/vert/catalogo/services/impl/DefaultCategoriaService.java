package com.vert.catalogo.services.impl;

import org.springframework.stereotype.Service;

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
    public Integer crearCategoria(Categoria cat) {
        return repository.nuevaCategoria(cat.getNombre());
    }

    @Override
    public Categoria buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + id));
    }

    @Override
    public Categoria buscarCategoria(String nombre) {
        return repository.findByNombre(nombre)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con nombre: " + nombre));
    }

    @Override
    public void borrarCategoria(Categoria cat) {
        Categoria existingCat = buscarPorId(cat.getId());
        repository.delete(existingCat);
    }

    @Override
    public void modificarCategoria(Categoria cat) {
        Categoria existingCat = buscarPorId(cat.getId());
        repository.modificarCategoria(cat.getId(), cat.getNombre());
    }

}
