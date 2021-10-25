package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple Utils Custom Exception tests
 *
 * @author Christian
 */
public class UtilsCustomExceptionsTest {
    @Test
    @DisplayName("Custom NotFoundException constructors test")
    void customNotFoundExceptionTest() {
        //empty constructor
        NotFoundException c1 = new NotFoundException();
        assertEquals(c1.getMessage(), null);
        NotFoundException c2 = new NotFoundException("Item not found");
        assertEquals(c2.getMessage(), "Item not found");
        NotFoundException c3 = new NotFoundException("Item not found", c2.getCause());
        assertEquals(c3.getMessage(), "Item not found");
        assertEquals(c3.getCause(), c2.getCause());
        NotFoundException c4 = new NotFoundException(c2.getCause());
        assertEquals(c4.getCause(), c2.getCause());
    }

    @Test
    @DisplayName("Custom InvalidInputException constructors test")
    void customInvalidInputExceptionTest() {
        //empty constructor
        InvalidInputException c1 = new InvalidInputException();
        assertEquals(c1.getMessage(), null);
        InvalidInputException c2 = new InvalidInputException("Invalid Item Format");
        assertEquals(c2.getMessage(), "Invalid Item Format");
        InvalidInputException c3 = new InvalidInputException("Invalid Item Format", c2.getCause());
        assertEquals(c3.getMessage(), "Invalid Item Format");
        assertEquals(c3.getCause(), c2.getCause());
        InvalidInputException c4 = new InvalidInputException(c2.getCause());
        assertEquals(c4.getCause(), c2.getCause());
    }
}
