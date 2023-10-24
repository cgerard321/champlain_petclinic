package com.petclinic.visits.visitsservicenew.Exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BadRequestExceptionTest {

    @Test
    void TestBadRequestString(){
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException("An Error Occurred");
        });
    }

}