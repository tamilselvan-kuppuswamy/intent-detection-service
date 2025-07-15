package com.jarvis.intentdetection.util;

import java.util.Set;

public class NoiseFilter {
  private static final Set<String> FILLERS =
      Set.of("", "hi", "hello", "okay", "uh", "um", "hmm", "huh", "what");

  public static boolean isNoise(String input) {
    String cleaned = input.toLowerCase().replaceAll("[^a-z]", "").trim();
    return cleaned.length() < 3 || FILLERS.contains(cleaned);
  }
}
