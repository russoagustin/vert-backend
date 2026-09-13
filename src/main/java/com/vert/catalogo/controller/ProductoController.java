package com.vert.catalogo.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vert.catalogo.dto.ProductoDto;
import com.vert.catalogo.services.interfaces.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDto>> listarProductos(
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idSubCategoria) {
        return ResponseEntity.ok(this.productoService.listarProductos(idCategoria, idSubCategoria));
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ProductoDto>> listarPorCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(this.productoService.listarPorCategoria(idCategoria));
    }

    @GetMapping("/categoria/{idCategoria}/subcategoria/{idSubCategoria}")
    public ResponseEntity<List<ProductoDto>> listarPorCategoriaYSubCategoria(
            @PathVariable Integer idCategoria,
            @PathVariable Integer idSubCategoria) {
        return ResponseEntity.ok(this.productoService.listarPorCategoriaYSubCategoria(idCategoria, idSubCategoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(this.productoService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ProductoDto> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(this.productoService.buscarPorNombre(nombre));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> crearProducto(
            @Valid @RequestPart("producto") ProductoDto productoDto,
            @RequestPart("imagen") MultipartFile imagen) {
        Integer newId = this.productoService.crearProducto(productoDto, imagen);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> modificarProductoConImagen(
            @PathVariable Integer id,
            @Valid @RequestPart("producto") ProductoDto productoDto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        this.productoService.modificarProducto(id, productoDto, imagen);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> modificarProductoSinImagen(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoDto productoDto) {
        this.productoService.modificarProducto(id, productoDto, null);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarProducto(@PathVariable Integer id) {
        this.productoService.borrarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
