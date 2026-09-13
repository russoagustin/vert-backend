package com.vert.catalogo.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vert.catalogo.exceptions.GlobalExceptionHandler;
import com.vert.catalogo.services.interfaces.CategoriaService;
import com.vert.catalogo.services.interfaces.ProductoService;
import com.vert.catalogo.services.interfaces.SubCategoriaService;

@ExtendWith(MockitoExtension.class)
class ValidationAndExceptionHandlerTest {

    private MockMvc mockMvcCategoria;
    private MockMvc mockMvcSubCategoria;
    private MockMvc mockMvcProducto;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private SubCategoriaService subCategoriaService;

    @Mock
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvcCategoria = MockMvcBuilders
                .standaloneSetup(new CategoriaController(categoriaService))
                .setControllerAdvice(exceptionHandler)
                .build();

        mockMvcSubCategoria = MockMvcBuilders
                .standaloneSetup(new SubCategoriaController(subCategoriaService))
                .setControllerAdvice(exceptionHandler)
                .build();

        mockMvcProducto = MockMvcBuilders
                .standaloneSetup(new ProductoController(productoService))
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void crearCategoria_NombreVacio_Retorna400ConValidationErrorYCampoNombre() throws Exception {
        String jsonBody = """
                {
                    "nombre": ""
                }
                """;

        mockMvcCategoria.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.mensaje.nombre", is("El nombre de la categoría no puede ser nulo ni estar vacío.")));
    }

    @Test
    void crearSubCategoria_CamposNulosYVacios_Retorna400ConTodosLosCamposConError() throws Exception {
        String jsonBody = """
                {
                    "idCategoria": null,
                    "nombre": "   "
                }
                """;

        mockMvcSubCategoria.perform(post("/api/subcategorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.mensaje.idCategoria", is("El ID de la categoría no puede ser nulo.")))
                .andExpect(jsonPath("$.mensaje.nombre", is("El nombre de la subcategoría no puede ser nulo ni estar vacío.")));
    }

    @Test
    void modificarProductoSinImagen_CamposInvalidos_Retorna400ConMultiplesErrores() throws Exception {
        String jsonBody = """
                {
                    "idCategoria": null,
                    "idSubCategoria": null,
                    "nombre": "",
                    "precio": -10.00,
                    "precioDescuento": -5.00,
                    "cantidad": -1
                }
                """;

        mockMvcProducto.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.mensaje.idCategoria", is("El ID de la categoría no puede ser nulo.")))
                .andExpect(jsonPath("$.mensaje.idSubCategoria", is("El ID de la subcategoría no puede ser nulo.")))
                .andExpect(jsonPath("$.mensaje.nombre", is("El nombre del producto no puede ser nulo ni estar vacío.")))
                .andExpect(jsonPath("$.mensaje.precio", is("El precio del producto debe ser mayor o igual a 0.")))
                .andExpect(jsonPath("$.mensaje.precioDescuento", is("El precio de descuento no puede ser negativo.")))
                .andExpect(jsonPath("$.mensaje.cantidad", is("La cantidad debe ser mayor o igual a 0.")));
    }

    @Test
    void modificarProductoSinImagen_PrecioDescuentoMayorQuePrecio_Retorna400ConErrorEnValidacion() throws Exception {
        String jsonBody = """
                {
                    "idCategoria": 1,
                    "idSubCategoria": 1,
                    "nombre": "Coca Cola",
                    "precio": 100.00,
                    "precioDescuento": 150.00,
                    "cantidad": 10
                }
                """;

        mockMvcProducto.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.mensaje.precioDescuentoValido", is("El precio de descuento no puede ser mayor al precio regular.")));
    }
}
