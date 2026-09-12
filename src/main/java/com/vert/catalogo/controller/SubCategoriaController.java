package com.vert.catalogo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vert.catalogo.dto.SubCategoriaDto;
import com.vert.catalogo.dto.SubCategoriaOrdenDto;
import com.vert.catalogo.services.interfaces.SubCategoriaService;

@RestController
@RequestMapping("/api/subcategorias")
public class SubCategoriaController {

    private final SubCategoriaService subCategoriaService;

    public SubCategoriaController(SubCategoriaService subCategoriaService) {
        this.subCategoriaService = subCategoriaService;
    }

    @GetMapping
    public ResponseEntity<List<SubCategoriaDto>> listarSubCategorias(@RequestParam(required = false) Integer idCategoria) {
        if (idCategoria != null) {
            return ResponseEntity.ok(subCategoriaService.listarPorCategoria(idCategoria));
        }
        return ResponseEntity.ok(subCategoriaService.listarSubCategorias());
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<SubCategoriaDto>> listarPorCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(subCategoriaService.listarPorCategoria(idCategoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategoriaDto> buscarPorId(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idCategoria) {
        return ResponseEntity.ok(subCategoriaService.buscarPorId(id, idCategoria));
    }

    @GetMapping("/categoria/{idCategoria}/{id}")
    public ResponseEntity<SubCategoriaDto> buscarPorCategoriaYId(
            @PathVariable Integer idCategoria,
            @PathVariable Integer id) {
        return ResponseEntity.ok(subCategoriaService.buscarPorId(id, idCategoria));
    }

    @GetMapping("/buscar")
    public ResponseEntity<SubCategoriaDto> buscarSubCategoria(
            @RequestParam Integer idCategoria,
            @RequestParam String nombre) {
        return ResponseEntity.ok(subCategoriaService.buscarSubCategoria(idCategoria, nombre));
    }

    @PostMapping
    public ResponseEntity<Void> crearSubCategoria(@RequestBody SubCategoriaDto subCategoriaDto) {
        Integer newId = subCategoriaService.crearSubCategoria(subCategoriaDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modificarSubCategoria(
            @PathVariable Integer id,
            @RequestBody SubCategoriaDto subCategoriaDto) {
        subCategoriaService.modificarSubCategoria(id, subCategoriaDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/orden")
    public ResponseEntity<Void> cambiarOrdenSubCategoria(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idCategoria,
            @RequestBody SubCategoriaDto subCategoriaDto) {
        Integer catId = idCategoria != null ? idCategoria : subCategoriaDto.idCategoria();
        subCategoriaService.cambiarOrdenSubCategoria(catId, id, subCategoriaDto.orden());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/orden")
    public ResponseEntity<Void> cambiarOrdenSubCategorias(
            @RequestParam Integer idCategoria,
            @RequestBody List<SubCategoriaOrdenDto> subcategorias) {
        subCategoriaService.cambiarOrdenSubCategorias(idCategoria, subcategorias);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categoria/{idCategoria}/orden")
    public ResponseEntity<Void> cambiarOrdenSubCategoriasPorCategoria(
            @PathVariable Integer idCategoria,
            @RequestBody List<SubCategoriaOrdenDto> subcategorias) {
        subCategoriaService.cambiarOrdenSubCategorias(idCategoria, subcategorias);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarSubCategoria(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idCategoria) {
        subCategoriaService.borrarSubCategoria(id, idCategoria);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categoria/{idCategoria}/{id}")
    public ResponseEntity<Void> borrarSubCategoriaPorCategoria(
            @PathVariable Integer idCategoria,
            @PathVariable Integer id) {
        subCategoriaService.borrarSubCategoria(id, idCategoria);
        return ResponseEntity.noContent().build();
    }
}
