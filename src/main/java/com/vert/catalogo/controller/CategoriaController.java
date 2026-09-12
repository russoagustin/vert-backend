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

import com.vert.catalogo.dto.CategoriaDto;
import com.vert.catalogo.dto.CategoriaOrdenDto;
import com.vert.catalogo.services.interfaces.CategoriaService;

@RestController
@RequestMapping("/api/categorias") // Puedes ajustar el path base según tus necesidades
public class CategoriaController {

    private final CategoriaService categoriaService;

    // Inyección de dependencias por constructor (recomendado)
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<CategoriaDto> buscarCategoria(@RequestParam String nombre) {
        return ResponseEntity.ok(categoriaService.buscarCategoria(nombre));
    }

    @PostMapping
    public ResponseEntity<Void> crearCategoria(@RequestBody CategoriaDto categoriaDto) {
        Integer newId = categoriaService.crearCategoria(categoriaDto);
        
        // Retorna 201 Created y el header Location con la URL del nuevo recurso
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();
                
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modificarCategoria(@PathVariable Integer id, @RequestBody CategoriaDto categoriaDto) {
        categoriaService.modificarCategoria(id, categoriaDto);
        return ResponseEntity.noContent().build(); // 204 No Content para actualizaciones exitosas
    }

    @PatchMapping("/{id}/orden")
    public ResponseEntity<Void> cambiarOrdenCategoria(@PathVariable Integer id, @RequestBody CategoriaDto categoriaDto) {
        categoriaService.cambiarOrdenCategoria(id, categoriaDto.orden());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/orden")
    public ResponseEntity<Void> cambiarOrdenCategorias(@RequestBody List<CategoriaOrdenDto> categorias) {
        categoriaService.cambiarOrdenCategorias(categorias);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarCategoria(@PathVariable Integer id) {
        categoriaService.borrarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
