package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class InvalidInputExceptionTest {

    @Test
    void TestInvalidInputEmptyConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException();
        });
    }

    @Test
    void TestInvalidInputString() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("Error");
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
            throw new InvalidInputException("Error", new Exception());
        });
    }

}
