package com.jarvis.intentdetection.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentResponse {
  private String intent;
  private int confidence;
  private String explanation;
}
