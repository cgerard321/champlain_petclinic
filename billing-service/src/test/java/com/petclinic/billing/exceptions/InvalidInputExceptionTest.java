package com.petclinic.billing.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class InvalidInputExceptionTest {
    @Test
    void TestInvalidInputEmptyConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException();
        });
    }

    @Test
    void TestInvalidInputCauseOnlyConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(new Exception());
        });
    }

    @Test
    void TestInvalidInputMessageAndCauseConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("Something went wrong", new Exception());
        });
    }
}