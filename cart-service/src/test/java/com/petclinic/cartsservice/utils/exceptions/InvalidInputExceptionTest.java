package com.petclinic.cartsservice.utils.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidInputExceptionTest {

    @Test
    void testInvalidInputException_DefaultConstructor() {
        InvalidInputException exception = new InvalidInputException();
        assertNull(exception.getMessage());
    }

    @Test
    void testInvalidInputException_WithMessage() {
        String message = "Invalid input!";
        InvalidInputException exception = new InvalidInputException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInvalidInputException_WithCause() {
        Throwable cause = new Throwable("Cause");
        InvalidInputException exception = new InvalidInputException(cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInvalidInputException_WithMessageAndCause() {
        String message = "Invalid input!";
        Throwable cause = new Throwable("Cause");
        InvalidInputException exception = new InvalidInputException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
