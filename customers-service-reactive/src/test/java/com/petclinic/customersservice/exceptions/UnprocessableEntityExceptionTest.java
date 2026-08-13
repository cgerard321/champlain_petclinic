package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnprocessableEntityExceptionTest {

    @Test
    void unprocessableEntityExceptionWithMessage_ShouldCreateExceptionWithMessage() {
        String message = "Entity cannot be processed";

        UnprocessableEntityException exception = new UnprocessableEntityException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
