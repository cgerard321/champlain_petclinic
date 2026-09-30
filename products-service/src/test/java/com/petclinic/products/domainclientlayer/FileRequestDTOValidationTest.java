package com.petclinic.products.domainclientlayer;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileRequestDTOValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void missingFileFieldsAreRejected() {
        FileRequestDTO request = FileRequestDTO.builder()
                .fileName(" ")
                .fileType(null)
                .fileData(new byte[0])
                .build();

        assertEquals(3, validator.validate(request).size());
    }
}
