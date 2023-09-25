package com.petclinic.visits.visitsservicenew.Exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.Not;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class NotFoundExceptionTest {

    @Test
    void TestNotFoundEmptyConstructor(){
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException();
        });
    }
    @Test
    void TestNotFoundString(){
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("An Error Occurred");
        });
    }
    @Test
    void TestNotFoundCauseOnlyConstructor(){
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException(new Exception());
        });
    }
    @Test
    void TestNotFoundMessageCauseConstructor(){
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("An error Occurred", new Exception());
        });
    }

}