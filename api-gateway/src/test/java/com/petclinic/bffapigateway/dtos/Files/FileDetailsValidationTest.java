package com.petclinic.bffapigateway.dtos.Files;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileDetailsValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void missingFileFieldsAreRejected() {
        FileDetails request = FileDetails.builder()
                .fileName("")
                .fileType(" ")
                .fileData(null)
                .build();

        assertEquals(3, validator.validate(request).size());
    }
}
