package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;

import java.util.Map;

public final class LocalizedContent {
    private LocalizedContent() { }

    // Older clients send one value; copy it to both languages for compatibility.
    public static Map<String, String> complete(Map<String, String> value, String legacy) {
        if (value == null) {
            if (legacy == null || legacy.isBlank()) {
                throw new InvalidInputException("English and French text are required.");
            }
            String text = legacy.trim();
            return Map.of("en", text, "fr", text);
        }
        String en = value.get("en");
        String fr = value.get("fr");
        if (en == null || en.isBlank() || fr == null || fr.isBlank()) {
            throw new InvalidInputException("English and French text are required.");
        }
        return Map.of("en", en.trim(), "fr", fr.trim());
    }

    public static String get(Map<String, String> value, String language, String legacy) {
        if (value == null) {
            return legacy;
        }
        return value.getOrDefault(language, legacy);
    }

    public static Map<String, String> stored(Map<String, String> value, String legacy) {
        if (value != null) {
            return value;
        }
        String text = legacy == null ? "" : legacy;
        return Map.of("en", text, "fr", text);
    }
}
