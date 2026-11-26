package com.example.nutrivida.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> manejarRecursoNoEncontrado(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Login con correo o contraseña incorrectos -> 401
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<String> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // Registro con un correo que ya existe -> 409
    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<String> manejarCorreoYaRegistrado(CorreoYaRegistradoException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Registro con un RUT que ya existe -> 409
    @ExceptionHandler(RutYaRegistradoException.class)
    public ResponseEntity<String> manejarRutYaRegistrado(RutYaRegistradoException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Datos inválidos (por ejemplo, registro sin nombre) -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarDatosInvalidos(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
