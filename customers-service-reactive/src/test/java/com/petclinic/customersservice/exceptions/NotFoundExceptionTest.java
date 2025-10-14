package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotFoundExceptionTest {

    @Test
    void NotFoundExceptionWithEmptyConstructorTest(){
        NotFoundException notFoundException = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException();
        });

        assertNull(notFoundException.getMessage());
    }

    @Test
    void NotFoundExceptionWithMessageTest(){
        String expectedMessage = "Appropriate NotFoundException message";
        NotFoundException notFoundException = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException(expectedMessage);
        });
         assertNotNull(notFoundException.getMessage());
        assertEquals(expectedMessage, notFoundException.getMessage());
    }

}
