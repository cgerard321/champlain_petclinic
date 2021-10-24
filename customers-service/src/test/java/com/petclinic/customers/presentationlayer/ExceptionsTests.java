package com.petclinic.customers.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.customerExceptions.http.GlobalControllerExceptionHandler;
import com.petclinic.customers.customerExceptions.http.HttpErrorInfo;
import com.petclinic.customers.datalayer.Owner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ExceptionsTests {

    @Autowired
    GlobalControllerExceptionHandler exceptionHandler;

    @Autowired
    ObjectMapper objectMapper;

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

    @Test
    void HttpErrorInfoWithNoConstructorTest(){
            HttpErrorInfo httpErrorInfo = new HttpErrorInfo();
            assertEquals(httpErrorInfo.getTimestamp(), null);
            assertEquals(httpErrorInfo.getHttpStatus(), null);
            assertEquals(httpErrorInfo.getPath(), null);
            assertEquals(httpErrorInfo.getMessage(), null);
    }

    @Test
    void HttpErrorInfoWithConstructorTest(){
        HttpErrorInfo httpErrorInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, "/owners/9999/", "Owner does not exist");
        assertNotNull(httpErrorInfo.getTimestamp());
        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.BAD_REQUEST);
        assertEquals(httpErrorInfo.getPath(), "/owners/9999/");
        assertEquals(httpErrorInfo.getMessage(), "Owner does not exist");
    }

    @Test
    void HandleNotFoundExceptionTest() throws JsonProcessingException {
        Owner newOwner = new Owner(1, "Wael", "Osman", "Address-1", "City-1", "1234567890");

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(MockServerHttpRequest.post("/owners", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newOwner)), new NotFoundException("Owner not found."));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.NOT_FOUND);
        assertEquals(httpErrorInfo.getMessage(), "Owner not found.");
    }

    @Test
    void HandleInvalidInputExceptionTest() throws JsonProcessingException {
        Owner newOwner = new Owner(1, "Wael", "Osman", "Address-1", "City-1", "1234567890");

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(MockServerHttpRequest.post("/owners", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newOwner)), new InvalidInputException("Owner doesn't exist."));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(httpErrorInfo.getPath(), "/owners");
        assertEquals(httpErrorInfo.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    }
}
