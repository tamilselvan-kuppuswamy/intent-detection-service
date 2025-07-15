package com.jarvis.intentdetection.util;

import java.util.HashMap;
import java.util.Map;

public class IntentUtils {

  private static final Map<String, String> canonical = new HashMap<>();
  private static final Map<String, String> aliases = new HashMap<>();

  static {
    // ✅ Canonical list (correct casing)
    canonical.put("createshipment", "CreateShipment");
    canonical.put("trackshipment", "TrackShipment");
    canonical.put("returnshipment", "ReturnShipment");
    canonical.put("rescheduledelivery", "RescheduleDelivery");
    canonical.put("reportissue", "ReportIssue");
    canonical.put("confirmintent", "ConfirmIntent");
    canonical.put("cancelintent", "CancelIntent");

    // 🔁 Alias keywords (can be overridden)
    aliases.put("ship", "CreateShipment");
    aliases.put("send", "CreateShipment");
    aliases.put("track", "TrackShipment");
    aliases.put("status", "TrackShipment");
    aliases.put("return", "ReturnShipment");
    aliases.put("reschedule", "RescheduleDelivery");
    aliases.put("issue", "ReportIssue");
    aliases.put("yes", "ConfirmIntent");
    aliases.put("no", "CancelIntent");
  }

  public static String normalize(String rawIntent) {
    if (rawIntent == null || rawIntent.isBlank()) return "Unknown";

    // Strip non-letters and lowercase for comparison
    String cleaned = rawIntent.trim().replaceAll("[^a-zA-Z]", "").toLowerCase();

    // Check canonical list first
    if (canonical.containsKey(cleaned)) {
      return canonical.get(cleaned);
    }

    // Then fallback to alias matching
    if (aliases.containsKey(cleaned)) {
      return aliases.get(cleaned);
    }

    // Last resort: title case it (e.g., "somethingelse" → "Somethingelse")
    return capitalize(cleaned);
  }

  private static String capitalize(String str) {
    return str.isEmpty()
        ? "Unknown"
        : str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }
}
