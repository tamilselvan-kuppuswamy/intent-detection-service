package com.jarvis.intentdetection.exception;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    String correlationId = MDC.get("correlationId");
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
    log.warn("❌ [CID:{}] Validation error: {}", correlationId, errors);
    return ResponseEntity.badRequest()
        .header("x-correlation-id", correlationId == null ? "" : correlationId)
        .body(errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleError(Exception ex) {
    String correlationId = MDC.get("correlationId");
    log.error("❌ [CID:{}] Unhandled error", correlationId, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header("x-correlation-id", correlationId == null ? "" : correlationId)
        .body(Map.of("error", "Internal server error"));
  }
}
