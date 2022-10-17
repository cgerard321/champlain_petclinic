package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class NotFoundExceptionTest {

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
