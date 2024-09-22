package com.petclinic.cartsservice.utils.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void testNotFoundException_DefaultConstructor() {
        NotFoundException exception = new NotFoundException();
        assertNull(exception.getMessage());
    }

    @Test
    void testNotFoundException_WithMessage() {
        String message = "Item not found!";
        NotFoundException exception = new NotFoundException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testNotFoundException_WithCause() {
        Throwable cause = new Throwable("Cause");
        NotFoundException exception = new NotFoundException(cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testNotFoundException_WithMessageAndCause() {
        String message = "Item not found!";
        Throwable cause = new Throwable("Cause");
        NotFoundException exception = new NotFoundException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
