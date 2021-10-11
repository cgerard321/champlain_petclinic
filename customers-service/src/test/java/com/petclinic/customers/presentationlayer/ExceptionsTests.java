package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
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
            throw new InvalidInputException("Appropriate InvalidInputException message");
        });
        assertNotNull(invalidInputException.getMessage());
    }

    @Test
    void NotFoundExceptionWithEmptyConstructorTest(){
        NotFoundException notFoundException = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException();
        });
        assertEquals(notFoundException.getMessage(), null);
    }

    @Test
    void NotFoundExceptionWithMessageTest(){
        NotFoundException notFoundException = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException("Appropriate NotFoundException message");
        });
        assertNotNull(notFoundException.getMessage());
    }
}
