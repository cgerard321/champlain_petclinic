package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BadRequestExceptionTest {

    @Test
    void badRequestExceptionWithMessage_ShouldCreateExceptionWithMessage() {
        String message = "Invalid request format";

        BadRequestException exception = new BadRequestException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
