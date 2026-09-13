package com.vert.catalogo.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vert.catalogo.dto.SubCategoriaDto;
import com.vert.catalogo.dto.SubCategoriaOrdenDto;
import com.vert.catalogo.entities.SubCategoria;
import com.vert.catalogo.exceptions.NotFoundException;
import com.vert.catalogo.exceptions.ValidationException;
import com.vert.catalogo.repositories.CategoriaRepository;
import com.vert.catalogo.repositories.SubCategoriaRepository;
import com.vert.catalogo.services.interfaces.SubCategoriaService;

@Service
public class DefaultSubCategoriaService implements SubCategoriaService {

    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaRepository categoriaRepository;

    public DefaultSubCategoriaService(
            SubCategoriaRepository subCategoriaRepository,
            CategoriaRepository categoriaRepository) {
        this.subCategoriaRepository = subCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<SubCategoriaDto> listarSubCategorias() {
        return this.subCategoriaRepository.findAllByOrderByCategoriaIdAscOrdenAsc()
                .stream()
                .map(SubCategoriaDto::fromEntity)
                .toList();
    }

    @Override
    public List<SubCategoriaDto> listarPorCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }

        this.categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + idCategoria));

        return this.subCategoriaRepository.findAllByCategoriaIdOrderByOrdenAsc(idCategoria)
                .stream()
                .map(SubCategoriaDto::fromEntity)
                .toList();
    }

    @Override
    public Integer crearSubCategoria(SubCategoriaDto subCat) {
        this.categoriaRepository.findById(subCat.idCategoria())
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + subCat.idCategoria()));

        return this.subCategoriaRepository.nuevaSubCategoria(subCat.idCategoria(), subCat.nombre().trim());
    }

    @Override
    public SubCategoriaDto buscarPorId(Integer id, Integer idCategoria) {
        if (id == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }

        SubCategoria subCat;
        if (idCategoria != null) {
            subCat = this.subCategoriaRepository.findByIdAndCategoriaId(id, idCategoria)
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
        } else {
            subCat = this.subCategoriaRepository.findAll().stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
        }

        return SubCategoriaDto.fromEntity(subCat);
    }

    @Override
    public SubCategoriaDto buscarSubCategoria(Integer idCategoria, String nombre) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("El nombre no puede ser nulo ni estar vacío.");
        }

        SubCategoria subCat = this.subCategoriaRepository.findByCategoriaIdAndNombre(idCategoria, nombre.trim())
                .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));

        return SubCategoriaDto.fromEntity(subCat);
    }

    @Override
    public void borrarSubCategoria(Integer id, Integer idCategoria) {
        if (id == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }

        SubCategoria subCat;
        if (idCategoria != null) {
            subCat = this.subCategoriaRepository.findByIdAndCategoriaId(id, idCategoria)
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
        } else {
            subCat = this.subCategoriaRepository.findAll().stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
        }

        this.subCategoriaRepository.delete(subCat);
    }

    @Override
    public void modificarSubCategoria(Integer id, SubCategoriaDto subCat) {
        if (id == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }
        if (subCat == null) {
            throw new ValidationException("Los datos de la subcategoría no pueden ser nulos.");
        }

        Integer catId = subCat.idCategoria();
        if (catId == null) {
            SubCategoria existing = this.subCategoriaRepository.findAll().stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
            catId = existing.getCategoria().getId();
        } else {
            this.subCategoriaRepository.findByIdAndCategoriaId(id, catId)
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));
        }

        this.subCategoriaRepository.modificarSubCategoria(catId, id, subCat.nombre());
    }

    @Override
    public void cambiarOrdenSubCategoria(Integer idCategoria, Integer idSubCategoria, Integer orden) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }
        if (idSubCategoria == null) {
            throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
        }
        if (orden == null) {
            throw new ValidationException("El orden de la subcategoría no puede ser nulo.");
        }

        this.subCategoriaRepository.findByIdAndCategoriaId(idSubCategoria, idCategoria)
                .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada"));

        this.subCategoriaRepository.cambiarOrdenSubCategoria(idCategoria, idSubCategoria, orden);
    }

    @Override
    @Transactional
    public void cambiarOrdenSubCategorias(Integer idCategoria, List<SubCategoriaOrdenDto> subcategorias) {
        if (idCategoria == null) {
            throw new ValidationException("El ID de la categoría no puede ser nulo.");
        }

        this.categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada con id: " + idCategoria));

        if (subcategorias == null || subcategorias.isEmpty()) {
            throw new ValidationException("La lista de subcategorías no puede ser nula ni estar vacía.");
        }

        this.normalizarOrdenes(subcategorias);

        Set<Integer> ordenes = new HashSet<>();
        Set<Integer> ids = new HashSet<>();

        for (SubCategoriaOrdenDto item : subcategorias) {
            if (item == null) {
                throw new ValidationException("El elemento de la lista no puede ser nulo.");
            }
            if (item.getId() == null) {
                throw new ValidationException("El ID de la subcategoría no puede ser nulo.");
            }
            if (item.getOrden() == null) {
                throw new ValidationException("El orden de la subcategoría no puede ser nulo.");
            }
            if (!ids.add(item.getId())) {
                throw new ValidationException("No se permiten IDs de subcategoría duplicados: " + item.getId());
            }
            if (!ordenes.add(item.getOrden())) {
                throw new ValidationException("No se permiten órdenes repetidos: orden " + item.getOrden());
            }
        }

        long totalSubCategorias = this.subCategoriaRepository.countByCategoriaId(idCategoria);
        if (subcategorias.size() != totalSubCategorias) {
            throw new ValidationException(
                    "Debe enviar el orden de todas las subcategorías de la categoría (" + totalSubCategorias + " subcategorías registradas).");
        }

        for (SubCategoriaOrdenDto item : subcategorias) {
            SubCategoria subCat = this.subCategoriaRepository.findByIdAndCategoriaId(item.getId(), idCategoria)
                    .orElseThrow(() -> new NotFoundException("SubCategoria no encontrada con id: " + item.getId() + " para la categoría: " + idCategoria));
            subCat.setOrden(item.getOrden());
            this.subCategoriaRepository.save(subCat);
        }
    }

    private void normalizarOrdenes(List<SubCategoriaOrdenDto> subcategorias) {
        List<SubCategoriaOrdenDto> listaMutable = new ArrayList<>(subcategorias);
        listaMutable.sort(Comparator.comparing(SubCategoriaOrdenDto::getOrden));
        for (int i = 0; i < listaMutable.size(); i++) {
            if (listaMutable.get(i).getOrden() != (i + 1)) {
                listaMutable.get(i).setOrden(i + 1);
            }
        }
    }
}
