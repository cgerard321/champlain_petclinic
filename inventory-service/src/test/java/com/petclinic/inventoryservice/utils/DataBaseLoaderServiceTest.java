package com.petclinic.inventoryservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataBaseLoaderServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void seedTextStoresBothLanguages() throws Exception {
        var translations = objectMapper.readTree("{\"First-Aid\":\"Premiers soins\"}");

        assertEquals(Map.of("en", "First-Aid", "fr", "Premiers soins"),
                DataBaseLoaderService.seedText(translations, "First-Aid"));
    }

    @Test
    void seedTextRejectsMissingFrenchTranslation() throws Exception {
        var translations = objectMapper.readTree("{}");

        assertThrows(IllegalStateException.class,
                () -> DataBaseLoaderService.seedText(translations, "First-Aid"));
    }

    @Test
    void migrationRestoresKnownStarterTranslation() throws Exception {
        var translations = objectMapper.readTree("{\"First-Aid\":\"Premiers soins\"}");

        assertEquals(Map.of("en", "First-Aid", "fr", "Premiers soins"),
                DataBaseLoaderService.migratedText(null, translations, "First-Aid"));
    }

    @Test
    void migrationPreservesStoredStaffTranslations() throws Exception {
        var translations = objectMapper.readTree("{\"First-Aid\":\"Premiers soins\"}");
        var stored = Map.of("en", "Staff kit", "fr", "Trousse personnalisée");

        assertSame(stored, DataBaseLoaderService.migratedText(stored, translations, "First-Aid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"Custom text\":null}", "{\"Custom text\":42}",
            "{\"Custom text\":\"   \"}"})
    void migrationCopiesLegacyTextWhenNoUsableTranslationExists(String dictionary) throws Exception {
        assertEquals(Map.of("en", "Custom text", "fr", "Custom text"),
                DataBaseLoaderService.migratedText(null, objectMapper.readTree(dictionary), "Custom text"));
    }

    @Test
    void migrationRetainsExistingNullTextFallback() throws Exception {
        assertEquals(Map.of("en", "", "fr", ""),
                DataBaseLoaderService.migratedText(null, objectMapper.readTree("{}"), null));
    }
}
