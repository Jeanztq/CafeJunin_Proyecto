package pe.cafejunin.adapters.in.rest;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.cafejunin.application.usecases.*;

@RestControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(ProhibidoException.class)
  ResponseEntity<?> prohibido(RuntimeException e) {
    return error(403, e.getMessage());
  }

  @ExceptionHandler(pe.cafejunin.adapters.out.prediction.PrediccionNoDisponibleException.class)
  ResponseEntity<?> predictor(RuntimeException e) {
    return error(503, "Predicción no disponible; no se guardó una evaluación");
  }

  @ExceptionHandler(org.springframework.dao.DataAccessException.class)
  ResponseEntity<?> base(Exception e) {
    return error(503, "Persistencia no disponible; revise la configuración del servidor");
  }

  @ExceptionHandler(NoEncontradoException.class)
  ResponseEntity<?> noEncontrado(RuntimeException e) {
    return error(404, e.getMessage());
  }

  @ExceptionHandler(ConflictoException.class)
  ResponseEntity<?> conflicto(RuntimeException e) {
    return error(409, e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> restriccion(Exception e) {
    return error(409, "Conflicto de datos: identificador duplicado o referencia inválida");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> invalido(RuntimeException e) {
    return error(400, e.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ResponseEntity<?> entrada(Exception e) {
    return error(400, "Datos inválidos: revise campos, tipos y rangos del contrato API");
  }

  private ResponseEntity<?> error(int status, String message) {
    return ResponseEntity.status(status).body(Map.of("estado", status, "mensaje", message));
  }
}
