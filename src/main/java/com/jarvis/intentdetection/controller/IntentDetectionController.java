package com.jarvis.intentdetection.controller;

import com.jarvis.intentdetection.dto.IntentRequest;
import com.jarvis.intentdetection.dto.IntentResponse;
import com.jarvis.intentdetection.service.IntentDetectionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/detect-intent")
@RequiredArgsConstructor
@Slf4j
public class IntentDetectionController {

  private final IntentDetectionService service;

  @PostMapping
  public ResponseEntity<IntentResponse> detect(
      @Valid @RequestBody IntentRequest request,
      @RequestHeader(value = "x-correlation-id", required = false) String correlationIdHeader) {
    String correlationId =
        (correlationIdHeader != null && !correlationIdHeader.isBlank())
            ? correlationIdHeader
            : UUID.randomUUID().toString();

    MDC.put("correlationId", correlationId);
    log.info("🔍 Received detect-intent request: {}", request.getText());

    IntentResponse response = service.detectIntent(request, correlationId);

    log.info(
        "✅ Intent detection completed: {} (confidence: {})",
        response.getIntent(),
        response.getConfidence());
    MDC.clear();

    return ResponseEntity.ok(response);
  }
}
