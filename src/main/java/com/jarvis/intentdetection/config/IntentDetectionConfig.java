package com.jarvis.intentdetection.config;

import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Configuration
@Component
@ConfigurationProperties(prefix = "intent-config")
public class IntentDetectionConfig {
  private Set<String> allowedIntents;
  private int confidenceThreshold;
  private double temperature;
  private int maxTokens;
}
