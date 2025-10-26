package com.f25_team6.duet.common;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleRse(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
      .body(Map.of("status", ex.getStatusCode().value(), "error", ex.getReason()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIae(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("status", 400, "error", ex.getMessage()));
  }
}
