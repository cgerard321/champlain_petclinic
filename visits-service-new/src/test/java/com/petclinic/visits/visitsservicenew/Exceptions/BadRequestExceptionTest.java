package com.petclinic.visits.visitsservicenew.Exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BadRequestExceptionTest {

    @Test
    void TestBadRequestString(){
        assertThrows(BadRequestException.class, () -> {
            throw new BadRequestException("An Error Occurred");
        });
    }

}