package com.vert.catalogo.exceptions;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Capturamos las excepciones relacionadas con la base de datos
    @ExceptionHandler({ JpaSystemException.class, DataAccessException.class })
    public ResponseEntity<Map<String, String>> handleDatabaseExceptions(Exception ex) {

        // 1. Buscamos la excepción de SQL original (la causa raíz)
        Throwable cause = ex.getCause();
        while (cause != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        Map<String, String> errorResponse = new HashMap<>();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String mensajeError = "Ocurrió un error interno en la base de datos.";

        // 2. Si encontramos la SQLException, verificamos el SQLSTATE
        if (cause instanceof SQLException) {
            SQLException sqlEx = (SQLException) cause;

            // '45000' es el código estándar para excepciones definidas por el usuario
            // (SIGNAL)
            if ("45000".equals(sqlEx.getSQLState())) {
                // Aquí extraemos exactamente el texto: "Error: El nombre de la categoría..."
                mensajeError = sqlEx.getMessage();
                status = HttpStatus.BAD_REQUEST; // 400 Bad Request tiene más sentido para validaciones
            } else {
                // Para otros errores de BD (ej. pérdida de conexión, sintaxis)
                mensajeError = "Error de base de datos: " + sqlEx.getMessage();
            }
        }

        errorResponse.put("error", mensajeError);

        return new ResponseEntity<>(errorResponse, status);
    }

}
