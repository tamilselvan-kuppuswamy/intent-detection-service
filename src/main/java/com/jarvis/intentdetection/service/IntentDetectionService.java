package com.jarvis.intentdetection.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarvis.intentdetection.config.IntentDetectionConfig;
import com.jarvis.intentdetection.dto.IntentRequest;
import com.jarvis.intentdetection.dto.IntentResponse;
import com.jarvis.intentdetection.util.IntentUtils;
import com.jarvis.intentdetection.util.NoiseFilter;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntentDetectionService {

  private final IntentDetectionConfig config;
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper mapper = new ObjectMapper();

  @Value("${azure.openai.endpoint}")
  private String endpoint;

  @Value("${azure.openai.api-key}")
  private String apiKey;

  @Value("${azure.openai.deployment}")
  private String deployment;

  @Value("${azure.openai.api-version}")
  private String apiVersion;

  public IntentResponse detectIntent(IntentRequest request, String correlationId) {
    // Set correlation ID in MDC if not present
    if (MDC.get("correlationId") == null && correlationId != null) {
      MDC.put("correlationId", correlationId);
    }

    String userText = request.getText().trim().toLowerCase();
    if (NoiseFilter.isNoise(userText)) {
      log.warn("🧽 [CID:{}] Ignored input due to noise: '{}'", correlationId, userText);
      return new IntentResponse("Unknown", 0, "Input was considered noise or filler.");
    }

    try {
      String prompt = buildPrompt(userText);
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(apiKey);
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> payload =
          Map.of(
              "messages",
              List.of(
                  Map.of("role", "system", "content", systemInstruction()),
                  Map.of("role", "user", "content", prompt)),
              "temperature",
              config.getTemperature(),
              "top_p",
              1,
              "max_tokens",
              config.getMaxTokens());

      String url =
          String.format(
              "%s/openai/deployments/%s/chat/completions?api-version=%s",
              endpoint, deployment, apiVersion);

      ResponseEntity<String> response =
          restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        return parseResponse(response.getBody(), userText, correlationId);
      }

      log.error("❌ [CID:{}] GPT call failed: {}", correlationId, response.getStatusCode());
    } catch (Exception e) {
      log.error("💥 [CID:{}] GPT exception", correlationId, e);
    } finally {
      // Do not remove correlationId here; handled in controller
    }

    return IntentResponse.builder()
        .intent("Unknown")
        .confidence(0)
        .explanation("Failed to process")
        .build();
  }

  private IntentResponse parseResponse(String json, String originalText, String correlationId)
      throws Exception {
    JsonNode content = mapper.readTree(json).path("choices").get(0).path("message").path("content");
    String[] lines = content.asText().split("\\n");
    String intent = "Unknown", explanation = "No explanation", confidenceStr = "0";

    for (String line : lines) {
      if (line.toLowerCase().contains("intent")) intent = line.split(":")[1].trim();
      if (line.toLowerCase().contains("confidence"))
        confidenceStr = line.replaceAll("[^0-9]", "").trim();
      if (line.toLowerCase().contains("because")) explanation = line.trim();
    }

    intent = IntentUtils.normalize(intent);
    int confidence = confidenceStr.isBlank() ? 0 : Integer.parseInt(confidenceStr);
    String finalIntent = intent;
    boolean allowed =
        config.getAllowedIntents().stream().anyMatch(a -> a.equalsIgnoreCase(finalIntent));
    if (!allowed || confidence < config.getConfidenceThreshold()) {
      log.warn(
          "⚠️ [CID:{}] Rejected intent '{}' with confidence {}", correlationId, intent, confidence);
      return new IntentResponse("Unknown", confidence, "Low confidence or not allowed");
    }

    log.info("✅ [CID:{}] Detected intent: {} ({}%)", correlationId, intent, confidence);
    log.debug("📄 [CID:{}] Explanation: {}", correlationId, explanation);

    // Audit log
    log.info(
        "📝 [CID:{}] AUDIT — Text: '{}', Intent: {}, Confidence: {}, Time: {}",
        correlationId,
        originalText,
        intent,
        confidence,
        Instant.now());

    return new IntentResponse(intent, confidence, explanation);
  }

  private String systemInstruction() {
    return """
        You are a logistics assistant. Only respond with:
        {
          "intent": "CreateShipment",
          "confidence": 95,
          "explanation": "Detected due to keywords like 'send' and a destination"
        }""";
  }

  private String buildPrompt(String userText) {
    return """
        Classify this user's logistics intent. Only use these:
        - CreateShipment
        - TrackShipment
        - ReturnShipment
        - RescheduleDelivery
        - ReportIssue
        - ConfirmIntent
        - CancelIntent

        Provide your output strictly in JSON format as shown above.

        User input: "%s"
        """
        .formatted(userText);
  }
}
