package com.petclinic.customersservice.exceptions;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InvalidInputExceptionTest {

    @Test
    void TestInvalidInputEmptyConstructor() {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException();
        });

        assertNull(exception.getMessage());
    }

    @Test
    void TestInvalidInputString() {
        String message = "Validation failed";
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(message);
        });
        assertEquals(message, exception.getMessage());
    }

    @Test
    void TestInvalidInputCauseOnlyConstructor() {
        Exception cause = new Exception("Root cause");
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(cause);
        });
        assertEquals(cause, exception.getCause());
    }

    @Test
    void TestInvalidInputMessageAndCauseConstructor() {
        String message = "Input error with cause";
        Exception cause = new Exception("Root cause");
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(message, cause);
        });
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
