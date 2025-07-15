package com.jarvis.intentdetection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntentRequest {
  @NotBlank private String text;
}
