package com.educacionit.alumnos_api.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlumnoNoEncontradoException.class)
    public ProblemDetail manejarAlumnoNoEncontrado(AlumnoNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Alumno no encontrado");
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }

    @ExceptionHandler(LegajoDuplicadoException.class)
    public ProblemDetail manejarLegajoDuplicado(LegajoDuplicadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Legajo duplicado");
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarErroresDeValidacion(MethodArgumentNotValidException ex) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorCampo(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Uno o más campos no son válidos");
        problema.setTitle("Error de validación");
        problema.setProperty("errors", errores);
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail manejarErrorGenerico(Exception ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Contactá al administrador.");
        problema.setTitle("Error interno");
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }

    private record ErrorCampo(String campo, String mensaje) {}
}