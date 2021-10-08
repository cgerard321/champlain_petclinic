package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ExceptionsTests {

    @Test
    void InvalidInputExceptionWithEmptyConstructorTest(){
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class, ()->{
            throw new InvalidInputException();
        });
        assertEquals(invalidInputException.getMessage(), null);
    }

    @Test
    void InvalidInputExceptionWithMessageTest(){
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class, ()->{
            throw new InvalidInputException("Appropriate exception message");
        });
        assertNotNull(invalidInputException.getMessage());
    }
}
