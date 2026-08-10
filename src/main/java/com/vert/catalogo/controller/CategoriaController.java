package com.vert.catalogo.controller;

import com.vert.catalogo.dto.CategoriaDto;
import com.vert.catalogo.entities.Categoria;
import com.vert.catalogo.services.interfaces.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<Void> crearCategoria(@RequestBody CategoriaDto categoriaDto) {
        Integer id = categoriaService.crearCategoria(categoriaDto.toEntity());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> listarCategorias() {
        List<Categoria> categorias = categoriaService.listarCategorias();
        return ResponseEntity.ok(categorias.stream().map(CategoriaDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> buscarPorId(@PathVariable Integer id) {
        Categoria categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(CategoriaDto.fromEntity(categoria));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CategoriaDto> buscarPorNombre(@PathVariable String nombre) {
        Categoria categoria = categoriaService.buscarCategoria(nombre);
        return ResponseEntity.ok(CategoriaDto.fromEntity(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modificarCategoria(@PathVariable Integer id, @RequestBody CategoriaDto categoriaDto) {
        categoriaDto.setId(id);
        categoriaService.modificarCategoria(categoriaDto.toEntity());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarCategoria(@PathVariable Integer id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoriaService.borrarCategoria(categoria);
        return ResponseEntity.noContent().build();
    }
}
