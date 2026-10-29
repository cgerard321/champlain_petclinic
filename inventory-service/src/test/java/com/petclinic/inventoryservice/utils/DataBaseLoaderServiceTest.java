package com.petclinic.inventoryservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
